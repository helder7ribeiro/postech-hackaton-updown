// src/main/java/org/fiap/updown/application/port/driver/job/ExistsJobByIdUseCase.java
package org.fiap.updown.application.port.driven;

import java.util.UUID;

public interface ExistsJobByIdUseCase {
    boolean execute(UUID id);
}
