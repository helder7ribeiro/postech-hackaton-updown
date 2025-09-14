package org.fiap.updown.application.usecase;

import lombok.RequiredArgsConstructor;
import org.fiap.updown.application.port.driven.GetAppUserByUsernameUseCase;
import org.fiap.updown.application.port.driver.AppUserPersistencePort;
import org.fiap.updown.domain.exception.RecursoNaoEncontradoException;
import org.fiap.updown.domain.model.AppUser;

@RequiredArgsConstructor
public class GetAppUserByUsernameUseCaseImpl implements GetAppUserByUsernameUseCase {

    private final AppUserPersistencePort appUserPort;

    @Override
    public AppUser execute(String username) {
        return appUserPort.findByUsername(username)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado com username: " + username));
    }
}
