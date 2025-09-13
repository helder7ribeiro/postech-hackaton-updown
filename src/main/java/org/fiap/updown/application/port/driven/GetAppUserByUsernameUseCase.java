package org.fiap.updown.application.port.driven;

import org.fiap.updown.domain.model.AppUser;

public interface GetAppUserByUsernameUseCase {
    AppUser execute(String username);
}
