// src/main/java/org/fiap/updown/application/port/driver/job/GetJobByIdUseCase.java
package org.fiap.updown.application.port.driven;

import org.fiap.updown.domain.model.Job;

import java.util.UUID;

public interface GetJobByIdUseCase {
    Job execute(UUID id);
}
