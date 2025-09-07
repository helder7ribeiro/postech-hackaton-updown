package org.fiap.updown.infrastructure.adapter.persistence.entity;

import jakarta.persistence.*;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.fiap.updown.domain.model.JobStatus;
import org.hibernate.annotations.*;
import org.hibernate.annotations.Cache;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "job",
        indexes = {
                @Index(name = "idx_job_user_id", columnList = "user_id"),
                @Index(name = "idx_job_created_at", columnList = "created_at")
        }
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_job_app_user"))
    private AppUserEntity user;

    /**
     * Ex.: s3://bucket/input/...
     */
    @NotNull
    @Size(max = 512)
    @Column(name = "source_object", length = 512, nullable = false)
    private String sourceObject;

    /**
     * Ex.: s3://bucket/output/...
     */
    @Size(max = 512)
    @Column(name = "result_object", length = 512)
    private String resultObject;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32, nullable = false)
    private JobStatus status;

    @Column(name = "error_msg", columnDefinition = "text")
    private String errorMsg;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
