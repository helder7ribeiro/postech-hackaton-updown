package org.fiap.updown.infrastructure.adapter.storage;

import org.fiap.updown.bdd.config.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
class VideoStorageS3AdapterTest extends AbstractIntegrationTest {

    @Autowired
    private VideoStorageS3Adapter videoStorageS3Adapter;

    @Autowired
    private S3Client s3Client;

    @Autowired
    private S3StorageProperties s3StorageProperties;

    @Test
    void deveArmazenarVideoComSucessoNoS3() {
        UUID userId = UUID.randomUUID();
        String originalFilename = "test-video.mp4";
        String contentType = "video/mp4";
        InputStream videoData = new ByteArrayInputStream("conteudo do video".getBytes());

        String s3Path = videoStorageS3Adapter.store(userId, originalFilename, contentType, videoData);

        assertThat(s3Path)
                .isNotNull()
                .startsWith("s3://" + s3StorageProperties.getBucket() + "/input/");

        assertDoesNotThrow(() -> {
            URI uri = new URI(s3Path);
            String bucket = uri.getHost();
            String key = uri.getPath().substring(1);
            HeadObjectRequest request = HeadObjectRequest.builder().bucket(bucket).key(key).build();
            s3Client.headObject(request);
        });
    }

    @Test
    void deveLancarExcecaoQuandoInputStreamForInvalido() {
        UUID userId = UUID.randomUUID();
        String originalFilename = "invalid-stream.mp4";
        String contentType = "video/mp4";
        InputStream videoData = null;

        assertThrows(NullPointerException.class, () -> {
            videoStorageS3Adapter.store(userId, originalFilename, contentType, videoData);
        });
    }
}