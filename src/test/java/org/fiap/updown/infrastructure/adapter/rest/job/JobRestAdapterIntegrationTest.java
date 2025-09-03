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

    @BeforeEach
    void setUp() {
        jobRepository.deleteAll();
        appUserRepository.deleteAll();

        testUser = appUserRepository.save(AppUserEntity.builder().email("usuario.job.api@teste.com").build());
        testJob = jobRepository.save(JobEntity.builder()
                .user(testUser)
                .sourceObject("s3://bucket/test/video.mp4")
                .status(JobStatus.RECEIVED)
                .build());
    }

    @Test
    void deveCriarJobComSucesso() throws Exception {
        // Arrange
        String payloadJson = String.format("{\"userId\":\"%s\"}", testUser.getId());
        MockMultipartFile payload = new MockMultipartFile("payload", "", "application/json", payloadJson.getBytes());
        MockMultipartFile video = new MockMultipartFile("video", "video.mp4", "video/mp4", "videocontent".getBytes());

        when(videoStorage.store(any(), any(), any(), any())).thenReturn("s3://mocked-path/video.mp4");

        mockMvc.perform(multipart("/api/v1/jobs")
                        .file(video)
                        .file(payload))
                .andExpect(status().isCreated())
                .andExpect(header().exists("X-updown-alert"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.status").value("RECEIVED"))
                .andExpect(jsonPath("$.user.id").value(testUser.getId().toString()));
    }

    @Test
    void deveBuscarJobPorId() throws Exception {
        mockMvc.perform(get("/api/v1/jobs/{id}", testJob.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testJob.getId().toString()))
                .andExpect(jsonPath("$.sourceObject").value("s3://bucket/test/video.mp4"))
                .andExpect(jsonPath("$.user.id").value(testUser.getId().toString()));
    }

    @Test
    void deveAtualizarJob() throws Exception {
        UpdateJobRequest request = new UpdateJobRequest(JobStatus.COMPLETED, "s3://bucket/output/video.mp4", null);

        mockMvc.perform(put("/api/v1/jobs/{id}", testJob.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(header().exists("X-updown-alert"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.resultObject").value("s3://bucket/output/video.mp4"));

        JobEntity updatedJob = jobRepository.findById(testJob.getId()).orElseThrow();
        assertThat(updatedJob.getStatus()).isEqualTo(JobStatus.COMPLETED);
    }

    @Test
    void deveDeletarJob() throws Exception {
        mockMvc.perform(delete("/api/v1/jobs/{id}", testJob.getId()))
                .andExpect(status().isNoContent())
                .andExpect(header().exists("X-updown-alert"));

        assertThat(jobRepository.existsById(testJob.getId())).isFalse();
    }

    @Test
    void deveVerificarSeJobExiste() throws Exception {
        mockMvc.perform(get("/api/v1/jobs/exists/{id}", testJob.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testJob.getId().toString()))
                .andExpect(jsonPath("$.exists").value(true));
    }
}