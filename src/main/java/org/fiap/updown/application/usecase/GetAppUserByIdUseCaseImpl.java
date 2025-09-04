// src/main/java/org/fiap/updown/application/usecase/appuser/GetAppUserByIdUseCaseImpl.java
package org.fiap.updown.application.usecase;

import lombok.RequiredArgsConstructor;
import org.fiap.updown.application.port.driven.GetAppUserByIdUseCase;
import org.fiap.updown.application.port.driver.AppUserPersistencePort;
import org.fiap.updown.domain.exception.RecursoNaoEncontradoException;
import org.fiap.updown.domain.model.AppUser;

import java.util.UUID;

@RequiredArgsConstructor
public class GetAppUserByIdUseCaseImpl implements GetAppUserByIdUseCase {

    private final AppUserPersistencePort appUserPort;

    @Override
    public AppUser execute(UUID id) {
        return appUserPort.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("AppUser não encontrado: " + id));
    }
}
