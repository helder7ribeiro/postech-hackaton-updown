// src/main/java/org/fiap/updown/application/port/driver/appuser/UpdateAppUserUseCase.java
package org.fiap.updown.application.port.driven;

import org.fiap.updown.domain.model.AppUser;

public interface UpdateAppUserUseCase {
    AppUser execute(AppUser toUpdate);
}
