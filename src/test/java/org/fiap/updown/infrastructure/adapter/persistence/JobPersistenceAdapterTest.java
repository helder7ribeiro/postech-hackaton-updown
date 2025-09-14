package org.fiap.updown.infrastructure.adapter.persistence;

import org.fiap.updown.domain.model.AppUser;
import org.fiap.updown.domain.model.Job;
import org.fiap.updown.domain.model.JobStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import({
        JobPersistenceAdapter.class,
        AppUserPersistenceAdapter.class,
        org.fiap.updown.infrastructure.adapter.persistence.mapper.JobMapperImpl.class,
        org.fiap.updown.infrastructure.adapter.persistence.mapper.AppUserMapperImpl.class
})
class JobPersistenceAdapterTest {

    @Autowired
    private JobPersistenceAdapter jobPersistenceAdapter;

    @Autowired
    private AppUserPersistenceAdapter appUserPersistenceAdapter;

    private AppUser testUser;

    @BeforeEach
    void setUp() {
        AppUser user = new AppUser();
        user.setEmail("usuario.job.teste@teste.com");
        user.setUsername("usuario.job.teste");
        testUser = appUserPersistenceAdapter.save(user);
    }

    @Test
    void deveSalvarEEncontrarJobPorId() {
        Job newJob = Job.builder()
                .user(testUser)
                .sourceObject("s3://bucket/source/video.mp4")
                .status(JobStatus.RECEIVED)
                .build();

        Job savedJob = jobPersistenceAdapter.save(newJob);
        Optional<Job> foundJobOpt = jobPersistenceAdapter.findById(savedJob.getId());

        assertThat(savedJob.getId()).isNotNull();
        assertThat(foundJobOpt).isPresent();

        Job foundJob = foundJobOpt.get();
        assertThat(foundJob.getId()).isEqualTo(savedJob.getId());
        assertThat(foundJob.getSourceObject()).isEqualTo("s3://bucket/source/video.mp4");
        assertThat(foundJob.getStatus()).isEqualTo(JobStatus.RECEIVED);
        assertThat(foundJob.getUser()).isNotNull();
        assertThat(foundJob.getUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    void deveAtualizarUmJob() {
        Job newJob = Job.builder()
                .user(testUser)
                .sourceObject("s3://bucket/source/video_to_update.mp4")
                .status(JobStatus.RECEIVED)
                .build();
        Job savedJob = jobPersistenceAdapter.save(newJob);
        UUID jobId = savedJob.getId();

        savedJob.setStatus(JobStatus.COMPLETED);
        savedJob.setResultObject("s3://bucket/output/completed.mp4");
        Job updatedJob = jobPersistenceAdapter.save(savedJob);

        Optional<Job> foundJobOpt = jobPersistenceAdapter.findById(jobId);
        assertThat(foundJobOpt).isPresent();
        assertThat(updatedJob.getStatus()).isEqualTo(JobStatus.COMPLETED);
        assertThat(updatedJob.getResultObject()).isEqualTo("s3://bucket/output/completed.mp4");
        assertThat(foundJobOpt.get().getStatus()).isEqualTo(JobStatus.COMPLETED);
    }

    @Test
    void deveVerificarExistenciaDeJobPorId() {
        Job newJob = Job.builder()
                .user(testUser)
                .sourceObject("s3://bucket/source/video_to_check.mp4")
                .status(JobStatus.RECEIVED)
                .build();
        Job savedJob = jobPersistenceAdapter.save(newJob);

        boolean existe = jobPersistenceAdapter.existsById(savedJob.getId());
        boolean naoExiste = jobPersistenceAdapter.existsById(UUID.randomUUID());

        assertThat(existe).isTrue();
        assertThat(naoExiste).isFalse();
    }

    @Test
    void deveDeletarJobPorId() {
        Job newJob = Job.builder()
                .user(testUser)
                .sourceObject("s3://bucket/source/video_to_delete.mp4")
                .status(JobStatus.RECEIVED)
                .build();
        Job savedJob = jobPersistenceAdapter.save(newJob);
        UUID jobId = savedJob.getId();
        assertThat(jobPersistenceAdapter.existsById(jobId)).isTrue();

        jobPersistenceAdapter.deleteById(jobId);

        assertThat(jobPersistenceAdapter.existsById(jobId)).isFalse();
    }
}