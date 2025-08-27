package org.fiap.updown.infrastructure.adapter.rest.job.dto;

import org.fiap.updown.domain.model.JobStatus;

import java.util.UUID;

public record JobResponse(
        UUID id,
        JobUserResponse user,
        String sourceObject,
        String resultObject,
        JobStatus status,
        String errorMsg
) {}