package org.fiap.updown.infrastructure.adapter.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.fiap.updown.application.port.driver.JobPersistencePort;
import org.fiap.updown.domain.model.Job;
import org.fiap.updown.domain.model.JobStatus;
import org.fiap.updown.infrastructure.adapter.persistence.entity.JobEntity;
import org.fiap.updown.infrastructure.adapter.persistence.mapper.AppUserMapper;
import org.fiap.updown.infrastructure.adapter.persistence.mapper.JobMapper;
import org.fiap.updown.infrastructure.adapter.persistence.repository.JobRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class  JobPersistenceAdapter implements JobPersistencePort {

    private final JobRepository jobRepository;
    private final JobMapper jobMapper;           // entidade <-> domínio
    private final AppUserMapper appUserMapper;   // usado se precisar refs leves de usuário

    // -------------------------------------------------------------------------
    // CRUD básico
    // -------------------------------------------------------------------------
    @Override
    @Transactional
    public Job save(Job job) {
        // JobMapper já mapeia user como referência leve (qualifiedByName = "appUserRef")
        JobEntity entity = jobMapper.toEntity(job);
        JobEntity saved = jobRepository.save(entity);
        return jobMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Job> findById(UUID id) {
        return jobRepository.findById(id).map(jobMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Job> findByIdAndUserId(UUID id, UUID userId) {
        return jobRepository.findByIdAndUser_Id(id, userId).map(jobMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        return jobRepository.existsById(id);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        jobRepository.deleteById(id);
    }

    // -------------------------------------------------------------------------
    // Consultas
    // -------------------------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public Page<Job> findByUserId(UUID userId, Pageable pageable) {
        return jobRepository.findByUser_Id(userId, pageable).map(jobMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Job> findTop20ByUserIdOrderByCreatedAtDesc(UUID userId) {
        return jobRepository.findTop20ByUser_IdOrderByCreatedAtDesc(userId)
                .stream().map(jobMapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Job> findByStatus(JobStatus status, Pageable pageable) {
        return jobRepository.findByStatus(status, pageable).map(jobMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Job> findByCreatedAtBetween(Instant start, Instant end) {
        return jobRepository.findByCreatedAtBetween(start, end)
                .stream().map(jobMapper::toDomain).toList();
    }

    // -------------------------------------------------------------------------
    // Stream para processamento em lote
    // -------------------------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public Stream<Job> streamAllByStatus(JobStatus status) {
        // IMPORTANTE: o chamador deve fechar o Stream (try-with-resources)
        Stream<JobEntity> stream = jobRepository.streamAllByStatus(status);
        return stream.map(jobMapper::toDomain);
    }

    // -------------------------------------------------------------------------
    // Updates parciais (sem carregar a entidade)
    // -------------------------------------------------------------------------
    @Override
    @Transactional
    public boolean updateStatusAndResult(UUID id, JobStatus status, String resultObject) {
        int updated = jobRepository.updateStatusAndResult(id, status, resultObject);
        return updated > 0;
    }

    @Override
    @Transactional
    public boolean updateStatusAndError(UUID id, JobStatus status, String errorMsg) {
        int updated = jobRepository.updateStatusAndError(id, status, errorMsg);
        return updated > 0;
    }
}
