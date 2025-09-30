// src/main/java/org/fiap/updown/application/port/driver/job/UpdateJobUseCase.java
package org.fiap.updown.application.port.driven;

import org.fiap.updown.domain.model.Job;

public interface UpdateJobUseCase {
    Job execute(Job toUpdate);
}
