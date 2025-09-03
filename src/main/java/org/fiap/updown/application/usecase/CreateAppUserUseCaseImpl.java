// src/main/java/org/fiap/updown/application/usecase/appuser/CreateAppUserUseCaseImpl.java
package org.fiap.updown.application.usecase;

import lombok.RequiredArgsConstructor;
import org.fiap.updown.application.port.driven.CreateAppUserUseCase;
import org.fiap.updown.application.port.driver.AppUserPersistencePort;
import org.fiap.updown.domain.exception.ConflitoDeDadosException;
import org.fiap.updown.domain.model.AppUser;

@RequiredArgsConstructor
public class CreateAppUserUseCaseImpl implements CreateAppUserUseCase {

    private final AppUserPersistencePort appUserPort;

    @Override
    public AppUser execute(AppUser toCreate) {
        if (appUserPort.existsByEmail(toCreate.getEmail())) {
            throw new ConflitoDeDadosException("E-mail já cadastrado: " + toCreate.getEmail());
        }
        return appUserPort.save(toCreate);
    }
}
