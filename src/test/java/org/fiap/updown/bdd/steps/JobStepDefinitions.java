package org.fiap.updown.bdd.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Quando;
import org.fiap.updown.bdd.TestContext;
import org.fiap.updown.bdd.config.AbstractIntegrationTest;
import org.fiap.updown.infrastructure.adapter.persistence.entity.AppUserEntity;
import org.fiap.updown.infrastructure.adapter.persistence.entity.JobEntity;
import org.fiap.updown.infrastructure.adapter.persistence.repository.AppUserRepository;
import org.fiap.updown.infrastructure.adapter.persistence.repository.JobRepository;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;

import java.net.URI;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@AutoConfigureMockMvc
public class JobStepDefinitions extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private S3Client s3Client;

    @Autowired
    private TestContext testContext;

    private JobEntity lastCreatedJob;

    private AppUserEntity ensureUserExists(String email) {
        return appUserRepository.findByEmailIgnoreCase(email)
                .orElseGet(() -> {
                    String username = email.split("@")[0];
                    return appUserRepository.save(AppUserEntity.builder()
                            .email(email)
                            .username(username)
                            .build());
                });
    }

    private String createMockJwtToken(String username) {
        // Para testes, vamos criar um token JWT mock simples
        // Em um ambiente real, você usaria uma biblioteca como jjwt ou nimbus-jose-jwt
        return "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImNvZ25pdG86dXNlcm5hbWUiOiI" + username + "In0.signature";
    }

    @Dado("que o usuário {string} criou um job")
    public void que_o_usuario_criou_um_job(String email) {
        AppUserEntity user = ensureUserExists(email);
        JobEntity job = JobEntity.builder()
                .user(user)
                .sourceObject("s3://videos/pre-existing/fake.mp4")
                .status(org.fiap.updown.domain.model.JobStatus.RECEIVED)
                .build();
        lastCreatedJob = jobRepository.save(job);
    }

    @Quando("o usuário {string} envia um vídeo para criar um job")
    public void o_usuario_envia_um_video_para_criar_um_job(String email) throws Exception {
        AppUserEntity user = appUserRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalStateException("Usuário " + email + " não encontrado."));

        MockMultipartFile videoFile = new MockMultipartFile("video", "video.mp4", MediaType.MULTIPART_FORM_DATA_VALUE, "video".getBytes());
        
        // Mock JWT token com username do usuário
        String mockJwtToken = createMockJwtToken(user.getUsername());

        testContext.setResultActions(mockMvc.perform(multipart("/api/v1/jobs")
                .file(videoFile)
                .header("Authorization", "Bearer " + mockJwtToken)));

        if (testContext.getResultActions().andReturn().getResponse().getStatus() == 201) {
            String responseBody = testContext.getResultActions().andReturn().getResponse().getContentAsString();
            UUID jobId = UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());
            lastCreatedJob = jobRepository.findById(jobId).orElse(null);
        }
    }

    @Quando("um usuário inexistente envia um vídeo para criar um job")
    public void um_usuario_inexistente_envia_um_video_para_criar_um_job() throws Exception {
        MockMultipartFile videoFile = new MockMultipartFile("video", "video.mp4", MediaType.MULTIPART_FORM_DATA_VALUE, "video".getBytes());
        
        // Mock JWT token com username inexistente
        String mockJwtToken = createMockJwtToken("usuario_inexistente");
        
        testContext.setResultActions(mockMvc.perform(multipart("/api/v1/jobs")
                .file(videoFile)
                .header("Authorization", "Bearer " + mockJwtToken)));
    }

    @Quando("um usuário envia uma requisição GET para a URL do último job criado")
    public void um_usuario_envia_uma_requisicao_get_para_a_url_do_ultimo_job_criado() throws Exception {
        Assertions.assertNotNull(lastCreatedJob, "Nenhum job foi criado no passo 'Dado' para este cenário.");
        
        // Mock JWT token para GET
        String mockJwtToken = createMockJwtToken("usuario.job.busca");
        
        testContext.setResultActions(mockMvc.perform(get("/api/v1/jobs/" + lastCreatedJob.getId())
                .header("Authorization", "Bearer " + mockJwtToken)));
    }

    @Quando("um usuário envia uma requisição DELETE para a URL do último job criado")
    public void um_usuario_envia_uma_requisicao_delete_para_a_url_do_ultimo_job_criado() throws Exception {
        Assertions.assertNotNull(lastCreatedJob, "Nenhum job foi criado no passo 'Dado' para este cenário.");
        
        // Mock JWT token para DELETE
        String mockJwtToken = createMockJwtToken("usuario.job.delete");
        
        testContext.setResultActions(mockMvc.perform(delete("/api/v1/jobs/" + lastCreatedJob.getId())
                .header("Authorization", "Bearer " + mockJwtToken)));
    }

    @E("o corpo da resposta deve conter o e-mail do usuário {string}")
    public void o_corpo_da_resposta_deve_conter_o_email_do_usuario(String email) throws Exception {
        testContext.getResultActions().andExpect(jsonPath("$.user.email").value(email));
    }

    @E("um job para o usuário {string} deve existir no banco de dados")
    public void um_job_para_o_usuario_deve_existir_no_banco_de_dados(String email) {
        AppUserEntity user = appUserRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalStateException("Usuário " + email + " não encontrado."));
        boolean jobExists = jobRepository.findAll().stream().anyMatch(job -> job.getUser().getId().equals(user.getId()));
        Assertions.assertTrue(jobExists, "Nenhum job encontrado para o usuário " + email);
    }

    @E("o vídeo do job deve existir no S3")
    public void o_video_do_job_deve_existir_no_s3() throws Exception {
        Assertions.assertNotNull(lastCreatedJob, "Nenhum job foi criado para poder verificar o vídeo no S3.");
        String sourceObject = lastCreatedJob.getSourceObject();
        URI uri = new URI(sourceObject);
        String bucket = uri.getHost();
        String key = uri.getPath().substring(1);
        HeadObjectRequest request = HeadObjectRequest.builder().bucket(bucket).key(key).build();
        Assertions.assertDoesNotThrow(() -> s3Client.headObject(request), "O objeto do job não foi encontrado no S3.");
    }

    @E("o último job criado não deve mais existir no banco de dados")
    public void o_ultimo_job_criado_nao_deve_mais_existir_no_banco_de_dados() {
        Assertions.assertNotNull(lastCreatedJob, "Nenhum job foi criado para poder verificar sua exclusão.");
        Assertions.assertFalse(jobRepository.existsById(lastCreatedJob.getId()));
    }
}