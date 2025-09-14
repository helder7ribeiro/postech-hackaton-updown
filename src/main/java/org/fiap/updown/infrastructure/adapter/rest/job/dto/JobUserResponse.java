package org.fiap.updown.infrastructure.adapter.rest.job.dto;

import java.util.UUID;

public record JobUserResponse(
        UUID id,
        String email,
        String username
) {}