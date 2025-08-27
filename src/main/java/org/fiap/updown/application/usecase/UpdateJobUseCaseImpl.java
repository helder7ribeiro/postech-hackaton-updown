// src/main/java/org/fiap/updown/application/usecase/job/UpdateJobUseCaseImpl.java
package org.fiap.updown.application.usecase;

import lombok.RequiredArgsConstructor;

import org.fiap.updown.application.port.driven.UpdateJobUseCase;
import org.fiap.updown.application.port.driver.JobPersistencePort;
import org.fiap.updown.domain.model.Job;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateJobUseCaseImpl implements UpdateJobUseCase {

    private final JobPersistencePort jobPort;

    @Override
    public Job execute(Job toUpdate) {
        UUID id = toUpdate.getId();
        Job current = jobPort.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Job não encontrado: " + id));

        // aplica mudanças permitidas (status/result/error)
        if (toUpdate.getStatus() != null) current.setStatus(toUpdate.getStatus());
        if (toUpdate.getResultObject() != null) current.setResultObject(toUpdate.getResultObject());
        if (toUpdate.getErrorMsg() != null) current.setErrorMsg(toUpdate.getErrorMsg());

        return jobPort.save(current);
    }
}
