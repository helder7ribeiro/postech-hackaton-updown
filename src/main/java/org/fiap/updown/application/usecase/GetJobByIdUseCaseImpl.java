// src/main/java/org/fiap/updown/application/usecase/job/GetJobByIdUseCaseImpl.java
package org.fiap.updown.application.usecase;

import lombok.RequiredArgsConstructor;

import org.fiap.updown.application.port.driven.GetJobByIdUseCase;
import org.fiap.updown.application.port.driver.JobPersistencePort;
import org.fiap.updown.domain.model.Job;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetJobByIdUseCaseImpl implements GetJobByIdUseCase {

    private final JobPersistencePort jobPort;

    @Override
    public Job execute(UUID id) {
        return jobPort.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Job não encontrado: " + id));
    }
}
