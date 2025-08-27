// src/main/java/org/fiap/updown/application/usecase/appuser/DeleteAppUserUseCaseImpl.java
package org.fiap.updown.application.usecase;

import lombok.RequiredArgsConstructor;
import org.fiap.updown.application.port.driven.DeleteAppUserUseCase;
import org.fiap.updown.application.port.driver.AppUserPersistencePort;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteAppUserUseCaseImpl implements DeleteAppUserUseCase {

    private final AppUserPersistencePort appUserPort;

    @Override
    public void execute(UUID id) {
        if (!appUserPort.existsById(id)) {
            throw new IllegalArgumentException("AppUser não encontrado: " + id);
        }
        appUserPort.deleteById(id);
    }
}
