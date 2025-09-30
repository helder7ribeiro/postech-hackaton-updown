// src/main/java/org/fiap/updown/application/port/driver/job/CreateJobUseCase.java
package org.fiap.updown.application.port.driven;


import org.fiap.updown.domain.CreateJobCommand;
import org.fiap.updown.domain.model.Job;

public interface CreateJobUseCase {
    Job execute(CreateJobCommand command);
}
