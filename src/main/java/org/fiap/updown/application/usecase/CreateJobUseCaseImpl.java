// src/main/java/org/fiap/updown/application/usecase/job/CreateJobUseCaseImpl.java
package org.fiap.updown.application.usecase;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.fiap.updown.application.port.driven.CreateJobUseCase;
import org.fiap.updown.application.port.driven.GetAppUserByUsernameUseCase;
import org.fiap.updown.application.port.driver.EventPublisher;
import org.fiap.updown.application.port.driver.JobPersistencePort;
import org.fiap.updown.application.port.driver.VideoStorage;
import org.fiap.updown.domain.CreateJobCommand;
import org.fiap.updown.domain.exception.FalhaInfraestruturaException;
import org.fiap.updown.domain.model.AppUser;
import org.fiap.updown.domain.model.Job;
import org.fiap.updown.domain.service.JobService;

@RequiredArgsConstructor
public class CreateJobUseCaseImpl implements CreateJobUseCase {

    private final GetAppUserByUsernameUseCase getAppUserByUsernameUseCase;
    private final JobPersistencePort jobPort;
    private final EventPublisher eventPublisher;
    private final VideoStorage videoStorage;
    private final JobService jobService;

    @Override
    @Transactional
    public Job execute(CreateJobCommand cmd) {
        AppUser owner = getAppUserByUsernameUseCase.execute(cmd.username());

        String sourceObject;
        try {
            sourceObject = videoStorage.store(owner.getId(), cmd.originalFilename(), cmd.contentType(), cmd.data());
        } catch (Exception e) {
            throw new FalhaInfraestruturaException("Falha ao armazenar vídeo: " + e.getMessage(), e);
        }

        Job jobCompleto = jobService.createJob(owner, sourceObject);
        Job saved = jobPort.save(jobCompleto);
        saved.setUser(owner);
        eventPublisher.novoVideoRecebido(saved);
        saved.setUser(owner);

        return saved;
    }
}
