package org.fiap.updown.infrastructure.adapter.storage;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fiap.updown.application.port.driver.VideoStorage;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class VideoStorageS3Adapter implements VideoStorage {

    private final S3Client s3;
    private final S3StorageProperties props;
    private final Clock clock = Clock.systemUTC();

    @PostConstruct
    void ensureBucket() {
        if (!props.isCreateBucketIfMissing()) return;
        String bucket = props.getBucket();
        try {
            s3.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
            log.info("Bucket S3 já existe: {}", bucket);
        } catch (S3Exception e) {
            log.info("Criando bucket S3: {}", bucket);
            CreateBucketRequest.Builder cb = CreateBucketRequest.builder().bucket(bucket);
            if (!"us-east-1".equals(props.getRegion())) {
                cb = cb.createBucketConfiguration(CreateBucketConfiguration.builder()
                        .locationConstraint(props.getRegion())
                        .build());
            }
            s3.createBucket(cb.build());
        }
    }

    /**
     * @param userId            dono do vídeo (UUID)
     * @param originalFilename  nome original (pode ser null)
     * @param contentType       MIME (pode ser null)
     * @param data              stream de vídeo
     * @return caminho lógico, ex.: s3://bucket/input/{userId}/{uuid}.mp4
     */
    @Override
    public String store(UUID userId, String originalFilename, String contentType, InputStream data) {
        String bucket = props.getBucket();
        String key = buildKey(userId, originalFilename);

        // Copia para temp file para conhecer o tamanho (AWS SDK v2 exige length em streams)
        Path tmp = null;
        try {
            tmp = Files.createTempFile("upload-video-", getExtension(originalFilename));
            long bytes = Files.copy(data, tmp, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            PutObjectRequest putReq = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType != null ? contentType : "application/octet-stream")
                    .contentLength(bytes)
                    .build();

            s3.putObject(putReq, RequestBody.fromFile(tmp));

            String uri = "s3://" + bucket + "/" + key;
            log.info("Vídeo salvo no S3: {} ({} bytes)", uri, bytes);
            return uri;

        } catch (IOException e) {
            throw new RuntimeException("Falha ao salvar vídeo no S3", e);
        } finally {
            if (tmp != null) {
                try { Files.deleteIfExists(tmp); } catch (IOException ignore) {}
            }
            try { if (data != null) data.close(); } catch (IOException ignore) {}
        }
    }

    private String buildKey(UUID userId, String originalFilename) {
        String sanitizedExt = getExtension(originalFilename); // ex.: ".mp4"
        String date = DateTimeFormatter.ofPattern("yyyy/MM/dd", Locale.ROOT).format(clock.instant().atZone(clock.getZone()));
        String fileId = UUID.randomUUID().toString();
        String prefix = trimSlashes(props.getInputPrefix()); // "input"
        return String.format("%s/%s/%s/%s/%s%s",
                prefix, userId, date, shortUuid(userId), fileId, sanitizedExt);
    }

    private String getExtension(String name) {
        if (name == null) return ".bin";
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) return ".bin";
        String ext = name.substring(dot).toLowerCase(Locale.ROOT);
        // sanity check simples
        if (ext.length() > 10) return ".bin";
        return ext.replaceAll("[^a-z0-9.]", "");
    }

    private String trimSlashes(String p) {
        if (p == null || p.isBlank()) return "";
        return p.replaceAll("^/+", "").replaceAll("/+$", "");
    }

    private String shortUuid(UUID u) {
        // só pra compor uma pasta curta (opcional)
        return u.toString().substring(0, 8);
    }
}
