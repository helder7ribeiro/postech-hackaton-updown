// src/main/java/org/fiap/updown/application/usecase/job/DeleteJobUseCaseImpl.java
package org.fiap.updown.application.usecase;

import lombok.RequiredArgsConstructor;
import org.fiap.updown.application.port.driven.DeleteJobUseCase;
import org.fiap.updown.application.port.driver.JobPersistencePort;
import org.fiap.updown.domain.exception.RecursoNaoEncontradoException;

import java.util.UUID;

@RequiredArgsConstructor
public class DeleteJobUseCaseImpl implements DeleteJobUseCase {

    private final JobPersistencePort jobPort;

    @Override
    public void execute(UUID id) {
        if (!jobPort.existsById(id)) {
            throw new RecursoNaoEncontradoException("Job não encontrado: " + id);
        }
        jobPort.deleteById(id);
    }
}
