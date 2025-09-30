package org.fiap.updown.infrastructure.adapter.persistence.repository;

import org.fiap.updown.domain.model.JobStatus;
import org.fiap.updown.infrastructure.adapter.persistence.entity.JobEntity;
import org.hibernate.jpa.HibernateHints;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.QueryHint;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface JobRepository extends JpaRepository<JobEntity, UUID>, JpaSpecificationExecutor<JobEntity> {

    // Página de jobs por usuário (FK)
    Page<JobEntity> findByUser_Id(UUID userId, Pageable pageable);

    // Últimos 20 jobs de um usuário
    List<JobEntity> findTop20ByUser_IdOrderByCreatedAtDesc(UUID userId);

    // Página por status
    Page<JobEntity> findByStatus(JobStatus status, Pageable pageable);

    // Intervalo de criação
    List<JobEntity> findByCreatedAtBetween(Instant start, Instant end);

    // Garantir que o job pertence ao usuário
    Optional<JobEntity> findByIdAndUser_Id(UUID id, UUID userId);

    // Stream para processar muitos registros (batch/worker)
    @Transactional(readOnly = true)
    @QueryHints(@QueryHint(name = HibernateHints.HINT_FETCH_SIZE, value = "100"))
    @Query("select j from JobEntity j where j.status = :status order by j.createdAt asc")
    Stream<JobEntity> streamAllByStatus(@Param("status") JobStatus status);

    // Atualiza status + resultObject (ex.: finalizar com sucesso)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update JobEntity j set j.status = :status, j.resultObject = :resultObject where j.id = :id")
    int updateStatusAndResult(@Param("id") UUID id,
                              @Param("status") JobStatus status,
                              @Param("resultObject") String resultObject);

    // Atualiza status + errorMsg (ex.: finalizar com erro)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update JobEntity j set j.status = :status, j.errorMsg = :errorMsg where j.id = :id")
    int updateStatusAndError(@Param("id") UUID id,
                             @Param("status") JobStatus status,
                             @Param("errorMsg") String errorMsg);
}
