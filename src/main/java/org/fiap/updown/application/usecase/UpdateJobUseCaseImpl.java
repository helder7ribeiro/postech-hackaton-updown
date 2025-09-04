// src/main/java/org/fiap/updown/application/usecase/job/UpdateJobUseCaseImpl.java
package org.fiap.updown.application.usecase;

import lombok.RequiredArgsConstructor;
import org.fiap.updown.application.port.driven.UpdateJobUseCase;
import org.fiap.updown.application.port.driver.JobPersistencePort;
import org.fiap.updown.domain.exception.DadosInvalidosException;
import org.fiap.updown.domain.exception.RecursoNaoEncontradoException;
import org.fiap.updown.domain.model.Job;

import java.util.UUID;

@RequiredArgsConstructor
public class UpdateJobUseCaseImpl implements UpdateJobUseCase {

    private final JobPersistencePort jobPort;

    @Override
    public Job execute(Job toUpdate) {
        if (toUpdate.getId() == null) {
            throw new DadosInvalidosException("ID do Job não pode ser nulo.");
        }

        UUID id = toUpdate.getId();
        Job current = jobPort.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Job não encontrado: " + id));

        // aplica mudanças permitidas (status/result/error)
        if (toUpdate.getStatus() != null) current.setStatus(toUpdate.getStatus());
        if (toUpdate.getResultObject() != null) current.setResultObject(toUpdate.getResultObject());
        if (toUpdate.getErrorMsg() != null) current.setErrorMsg(toUpdate.getErrorMsg());

        return jobPort.save(current);
    }
}
