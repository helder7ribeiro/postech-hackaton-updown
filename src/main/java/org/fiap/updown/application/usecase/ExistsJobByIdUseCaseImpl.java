// src/main/java/org/fiap/updown/application/usecase/job/ExistsJobByIdUseCaseImpl.java
package org.fiap.updown.application.usecase;

import lombok.RequiredArgsConstructor;
import org.fiap.updown.application.port.driven.ExistsJobByIdUseCase;
import org.fiap.updown.application.port.driver.JobPersistencePort;

import java.util.UUID;

@RequiredArgsConstructor
public class ExistsJobByIdUseCaseImpl implements ExistsJobByIdUseCase {

    private final JobPersistencePort jobPort;

    @Override
    public boolean execute(UUID id) {
        return jobPort.existsById(id);
    }
}
