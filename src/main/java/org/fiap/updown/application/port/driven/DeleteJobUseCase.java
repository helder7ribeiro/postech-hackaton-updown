// src/main/java/org/fiap/updown/application/port/driver/job/DeleteJobUseCase.java
package org.fiap.updown.application.port.driven;

import java.util.UUID;

public interface DeleteJobUseCase {
    void execute(UUID id);
}
