package org.fiap.updown.infrastructure.adapter.rest.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.fiap.updown.application.port.driver.EventPublisher;
import org.fiap.updown.application.port.driver.VideoStorage;
import org.fiap.updown.bdd.config.AbstractIntegrationTest;
import org.fiap.updown.domain.model.JobStatus;
import org.fiap.updown.infrastructure.adapter.persistence.entity.AppUserEntity;
import org.fiap.updown.infrastructure.adapter.persistence.entity.JobEntity;
import org.fiap.updown.infrastructure.adapter.persistence.repository.AppUserRepository;
import org.fiap.updown.infrastructure.adapter.persistence.repository.JobRepository;
import org.fiap.updown.infrastructure.adapter.rest.job.dto.UpdateJobRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@Transactional
class JobRestAdapterIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private JobRepository jobRepository;

    @MockitoBean
    private VideoStorage videoStorage;
    @MockitoBean
    private EventPublisher eventPublisher;

    private AppUserEntity testUser;
    private JobEntity testJob;

    private String createMockJwtToken(String username) {
        String header = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9";
        String payload = "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImNvZ25pdG86dXNlcm5hbWUiOiI" + username + "In0";
        String signature = "signature";
        return header + "." + payload + "." + signature;
    }

    @BeforeEach
    void setUp() {
        jobRepository.deleteAll();
        appUserRepository.deleteAll();

        testUser = appUserRepository.save(AppUserEntity.builder()
                .email("usuario.job.api@teste.com")
                .username("usuario.job.api")
                .build());
        testJob = jobRepository.save(JobEntity.builder()
                .user(testUser)
                .sourceObject("s3://bucket/test/video.mp4")
                .status(JobStatus.RECEIVED)
                .build());
    }

    @Test
    void deveCriarJobComSucesso() throws Exception {
        // Arrange
        MockMultipartFile video = new MockMultipartFile("video", "video.mp4", "video/mp4", "videocontent".getBytes());
        String mockJwtToken = createMockJwtToken(testUser.getUsername());

        when(videoStorage.store(any(), any(), any(), any())).thenReturn("s3://mocked-path/video.mp4");

        mockMvc.perform(multipart("/api/v1/jobs")
                        .file(video)
                        .header("Authorization", "Bearer " + mockJwtToken))
                .andExpect(status().isCreated())
                .andExpect(header().exists("X-updown-alert"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.status").value("RECEIVED"))
                .andExpect(jsonPath("$.user.id").value(testUser.getId().toString()));
    }

    @Test
    void deveBuscarJobPorId() throws Exception {
        String mockJwtToken = createMockJwtToken(testUser.getUsername());
        
        mockMvc.perform(get("/api/v1/jobs/{id}", testJob.getId())
                        .header("Authorization", "Bearer " + mockJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testJob.getId().toString()))
                .andExpect(jsonPath("$.sourceObject").value("s3://bucket/test/video.mp4"))
                .andExpect(jsonPath("$.user.id").value(testUser.getId().toString()));
    }

    @Test
    void deveAtualizarJob() throws Exception {
        UpdateJobRequest request = new UpdateJobRequest(JobStatus.COMPLETED, "s3://bucket/output/video.mp4", null);
        String mockJwtToken = createMockJwtToken(testUser.getUsername());

        mockMvc.perform(put("/api/v1/jobs/{id}", testJob.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + mockJwtToken))
                .andExpect(header().exists("X-updown-alert"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.resultObject").value("s3://bucket/output/video.mp4"));

        JobEntity updatedJob = jobRepository.findById(testJob.getId()).orElseThrow();
        assertThat(updatedJob.getStatus()).isEqualTo(JobStatus.COMPLETED);
    }

    @Test
    void deveDeletarJob() throws Exception {
        String mockJwtToken = createMockJwtToken(testUser.getUsername());
        
        mockMvc.perform(delete("/api/v1/jobs/{id}", testJob.getId())
                        .header("Authorization", "Bearer " + mockJwtToken))
                .andExpect(status().isNoContent())
                .andExpect(header().exists("X-updown-alert"));

        assertThat(jobRepository.existsById(testJob.getId())).isFalse();
    }

    @Test
    void deveVerificarSeJobExiste() throws Exception {
        String mockJwtToken = createMockJwtToken(testUser.getUsername());
        
        mockMvc.perform(get("/api/v1/jobs/exists/{id}", testJob.getId())
                        .header("Authorization", "Bearer " + mockJwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testJob.getId().toString()))
                .andExpect(jsonPath("$.exists").value(true));
    }
}