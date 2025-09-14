// src/main/java/org/fiap/updown/application/usecase/appuser/UpdateAppUserUseCaseImpl.java
package org.fiap.updown.application.usecase;

import lombok.RequiredArgsConstructor;
import org.fiap.updown.application.port.driven.UpdateAppUserUseCase;
import org.fiap.updown.application.port.driver.AppUserPersistencePort;
import org.fiap.updown.domain.exception.ConflitoDeDadosException;
import org.fiap.updown.domain.exception.RecursoNaoEncontradoException;
import org.fiap.updown.domain.model.AppUser;

import java.util.UUID;

@RequiredArgsConstructor
public class UpdateAppUserUseCaseImpl implements UpdateAppUserUseCase {

    private final AppUserPersistencePort appUserPort;

    @Override
    public AppUser execute(AppUser toUpdate) {
        UUID id = toUpdate.getId();
        AppUser current = appUserPort.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("AppUser não encontrado: " + id));

        if (!current.getEmail().equalsIgnoreCase(toUpdate.getEmail())
                && appUserPort.existsByEmail(toUpdate.getEmail())) {
            throw new ConflitoDeDadosException("E-mail já cadastrado: " + toUpdate.getEmail());
        }

        if (!current.getUsername().equalsIgnoreCase(toUpdate.getUsername())
                && appUserPort.existsByUsername(toUpdate.getUsername())) {
            throw new ConflitoDeDadosException("Username já cadastrado: " + toUpdate.getUsername());
        }

        current.setEmail(toUpdate.getEmail());
        current.setUsername(toUpdate.getUsername());
        return appUserPort.save(current);
    }
}
