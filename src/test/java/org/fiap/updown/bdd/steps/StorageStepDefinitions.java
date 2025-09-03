package org.fiap.updown.bdd.steps;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import org.fiap.updown.application.port.driver.VideoStorage;
import org.fiap.updown.bdd.config.AbstractIntegrationTest;
import org.fiap.updown.domain.exception.FalhaInfraestruturaException;
import org.fiap.updown.infrastructure.adapter.storage.S3StorageProperties;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

@AutoConfigureMockMvc
public class StorageStepDefinitions extends AbstractIntegrationTest {

    @Autowired
    private VideoStorage videoStorage;

    @Autowired
    private S3Client s3Client;

    @Autowired
    private S3StorageProperties s3StorageProperties;

    private String resultUri;
    private Exception thrownException;

    @Dado("que o serviço S3 está configurado e operacional")
    public void que_o_servico_s3_esta_configurado_e_operacional() {
        // Passo implícito, garantido pelo Testcontainers
    }

    @Quando("um usuário envia um vídeo chamado {string} do tipo {string}")
    public void um_usuario_envia_um_video(String filename, String contentType) {
        try {
            InputStream data = new ByteArrayInputStream("dados do video de teste".getBytes());
            resultUri = videoStorage.store(UUID.randomUUID(), filename, contentType, data);
        } catch (Exception e) {
            thrownException = e;
        }
    }

    @Quando("um usuário tenta enviar um vídeo")
    public void um_usuario_tenta_enviar_um_video() {
        um_usuario_envia_um_video("video_teste.mp4", "video/mp4");
    }

    @Quando("um usuário tenta enviar um vídeo com um stream de dados corrompido")
    public void um_usuario_tenta_enviar_um_video_corrompido() {
        try {
            InputStream corruptedStream = new InputStream() {
                @Override
                public int read() throws IOException {
                    throw new IOException("Simulated Stream Error");
                }
            };
            videoStorage.store(UUID.randomUUID(), "corrupted.mp4", "video/mp4", corruptedStream);
        } catch (Exception e) {
            thrownException = e;
        }
    }

    @Entao("o vídeo deve ser salvo no S3")
    public void o_video_deve_ser_salvo_no_s3() throws URISyntaxException {
        Assertions.assertNotNull(resultUri);

        URI uri = new URI(resultUri);
        String bucket = uri.getHost();
        String key = uri.getPath().substring(1);

        HeadObjectRequest request = HeadObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        Assertions.assertDoesNotThrow(() -> s3Client.headObject(request), "O objeto não foi encontrado no S3.");
    }

    @Entao("uma URI do S3 deve ser retornada")
    public void uma_uri_do_s3_deve_ser_retornada() {
        Assertions.assertNotNull(resultUri);
        Assertions.assertTrue(resultUri.startsWith("s3://" + s3StorageProperties.getBucket() + "/"));
    }

    @Entao("uma FalhaInfraestruturaException deve ser lançada")
    public void uma_falha_infraestrutura_exception_deve_ser_lancada() {
        Assertions.assertNotNull(thrownException);
        Assertions.assertInstanceOf(FalhaInfraestruturaException.class, thrownException);
    }
}

