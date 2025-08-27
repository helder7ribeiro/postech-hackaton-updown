// src/main/java/org/fiap/updown/application/usecase/appuser/GetAppUserByIdUseCaseImpl.java
package org.fiap.updown.application.usecase;

import lombok.RequiredArgsConstructor;
import org.fiap.updown.application.port.driven.GetAppUserByIdUseCase;
import org.fiap.updown.application.port.driver.AppUserPersistencePort;
import org.fiap.updown.domain.model.AppUser;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetAppUserByIdUseCaseImpl implements GetAppUserByIdUseCase {

    private final AppUserPersistencePort appUserPort;

    @Override
    public AppUser execute(UUID id) {
        return appUserPort.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("AppUser não encontrado: " + id));
    }
}
