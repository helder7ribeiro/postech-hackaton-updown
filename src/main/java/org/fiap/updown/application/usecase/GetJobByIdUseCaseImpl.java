// src/main/java/org/fiap/updown/application/usecase/job/GetJobByIdUseCaseImpl.java
package org.fiap.updown.application.usecase;

import lombok.RequiredArgsConstructor;
import org.fiap.updown.application.port.driven.GetJobByIdUseCase;
import org.fiap.updown.application.port.driver.JobPersistencePort;
import org.fiap.updown.domain.exception.RecursoNaoEncontradoException;
import org.fiap.updown.domain.model.Job;

import java.util.UUID;

@RequiredArgsConstructor
public class GetJobByIdUseCaseImpl implements GetJobByIdUseCase {

    private final JobPersistencePort jobPort;

    @Override
    public Job execute(UUID id) {
        return jobPort.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Job não encontrado: " + id));
    }
}
