// src/main/java/org/fiap/updown/application/usecase/appuser/ExistsAppUserByEmailUseCaseImpl.java
package org.fiap.updown.application.usecase;

import lombok.RequiredArgsConstructor;
import org.fiap.updown.application.port.driven.ExistsAppUserByEmailUseCase;
import org.fiap.updown.application.port.driver.AppUserPersistencePort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExistsAppUserByEmailUseCaseImpl implements ExistsAppUserByEmailUseCase {

    private final AppUserPersistencePort appUserPort;

    @Override
    public boolean execute(String email) {
        return appUserPort.existsByEmail(email);
    }
}
