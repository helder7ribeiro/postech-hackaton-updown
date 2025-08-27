package org.fiap.updown.infrastructure.adapter.rest.job.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateJobRequest(
        @NotNull(message = "userId é obrigatório")
        UUID userId
) {}