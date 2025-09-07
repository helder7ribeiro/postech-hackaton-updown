package org.fiap.updown.infrastructure.adapter.rest.job.dto;

import java.util.UUID;

public record JobExistsBySourceResponse(
        UUID userId,
        String sourceObject,
        boolean exists
) {}