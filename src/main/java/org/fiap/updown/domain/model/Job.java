package org.fiap.updown.domain.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Job {
    private UUID id;

    /** Dono do job (FK para app_user.id) */
    @NotNull(message = "Usuário é obrigatório")
    @Valid
    private AppUser user;

    /** Ex.: s3://bucket/input/... */
    @NotBlank(message = "sourceObject é obrigatório")
    @Size(max = 512, message = "sourceObject deve ter no máximo 512 caracteres")
    @Pattern(regexp = "^s3://[A-Za-z0-9.\\-]+/.+",
             message = "sourceObject deve ser um caminho S3 válido (ex.: s3://bucket/path)")
    private String sourceObject;

    /** Ex.: s3://bucket/output/... (opcional) */
    @Size(max = 512, message = "resultObject deve ter no máximo 512 caracteres")
    @Pattern(regexp = "^s3://[A-Za-z0-9.\\-]+/.+",
             message = "resultObject deve ser um caminho S3 válido (ex.: s3://bucket/path)")
    private String resultObject;

    @NotNull(message = "Status é obrigatório")
    private JobStatus status;

    /** Mensagem de erro (quando status=FAILED). */
    private String errorMsg;

    @PastOrPresent(message = "createdAt não pode estar no futuro")
    private Instant createdAt;

    @PastOrPresent(message = "updatedAt não pode estar no futuro")
    private Instant updatedAt;
}
