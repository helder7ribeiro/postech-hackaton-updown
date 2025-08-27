// src/main/java/org/fiap/updown/application/port/driver/appuser/CreateAppUserUseCase.java
package org.fiap.updown.application.port.driven;

import org.fiap.updown.domain.model.AppUser;

public interface CreateAppUserUseCase {
    AppUser execute(AppUser toCreate);
}
