// src/main/java/org/fiap/updown/application/port/driver/appuser/DeleteAppUserUseCase.java
package org.fiap.updown.application.port.driven;

import java.util.UUID;

public interface DeleteAppUserUseCase {
    void execute(UUID id);
}
