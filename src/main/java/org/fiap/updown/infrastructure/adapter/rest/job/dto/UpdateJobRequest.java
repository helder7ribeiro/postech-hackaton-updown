package org.fiap.updown.infrastructure.adapter.rest.job.dto;

import jakarta.validation.constraints.Size;
import org.fiap.updown.domain.model.JobStatus;

public record UpdateJobRequest(
        // normalmente só atualizamos estes campos:
        JobStatus status,
        @Size(max = 512) String resultObject,
        String errorMsg
) {}