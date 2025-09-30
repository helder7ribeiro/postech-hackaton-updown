// src/main/java/org/fiap/updown/application/port/driver/appuser/GetAppUserByIdUseCase.java
package org.fiap.updown.application.port.driven;

import org.fiap.updown.domain.model.AppUser;

import java.util.UUID;

public interface GetAppUserByIdUseCase {
    AppUser execute(UUID id);
}
