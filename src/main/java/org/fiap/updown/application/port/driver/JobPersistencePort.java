package org.fiap.updown.application.port.driver;

import org.fiap.updown.domain.model.Job;
import org.fiap.updown.domain.model.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public interface JobPersistencePort {

    Job save(Job job);

    Optional<Job> findById(UUID id);

    Optional<Job> findByIdAndUserId(UUID id, UUID userId);

    boolean existsById(UUID id);

    void deleteById(UUID id);

    Page<Job> findByUserId(UUID userId, Pageable pageable);

    List<Job> findTop20ByUserIdOrderByCreatedAtDesc(UUID userId);

    Page<Job> findByStatus(JobStatus status, Pageable pageable);

    List<Job> findByCreatedAtBetween(Instant start, Instant end);

    /**
     * Stream de jobs por status; o chamador deve fechar o stream.
     * Ideal para consumidores/batches.
     */
    Stream<Job> streamAllByStatus(JobStatus status);

    /** Atualiza status + resultObject via UPDATE direto (sem carregar a entidade). */
    boolean updateStatusAndResult(UUID id, JobStatus status, String resultObject);

    /** Atualiza status + errorMsg via UPDATE direto (sem carregar a entidade). */
    boolean updateStatusAndError(UUID id, JobStatus status, String errorMsg);
}
