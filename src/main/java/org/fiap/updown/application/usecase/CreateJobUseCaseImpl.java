// src/main/java/org/fiap/updown/application/usecase/job/CreateJobUseCaseImpl.java
package org.fiap.updown.application.usecase;

import lombok.RequiredArgsConstructor;

import org.fiap.updown.application.port.driven.CreateJobUseCase;
import org.fiap.updown.application.port.driver.AppUserPersistencePort;
import org.fiap.updown.application.port.driver.EventPublisher;
import org.fiap.updown.application.port.driver.JobPersistencePort;
import org.fiap.updown.application.port.driver.VideoStorage;
import org.fiap.updown.domain.CreateJobCommand;
import org.fiap.updown.domain.model.AppUser;
import org.fiap.updown.domain.model.Job;
import org.fiap.updown.domain.model.JobStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateJobUseCaseImpl implements CreateJobUseCase {

    private final AppUserPersistencePort appUserPort;
    private final JobPersistencePort jobPort;
    private final EventPublisher eventPublisher;
    private final VideoStorage videoStorage;

    @Override
    public Job execute(CreateJobCommand cmd) {

        AppUser owner = appUserPort.findById(cmd.userId())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + cmd.userId()));

        String sourceObject;
        try {
            sourceObject = videoStorage.store(cmd.userId(), cmd.originalFilename(), cmd.contentType(), cmd.data());
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao armazenar vídeo: " + e.getMessage(), e);
        }

        Job job = Job.builder()
                .user(AppUser.builder().id(owner.getId()).email(owner.getEmail()).build())
                .sourceObject(sourceObject)
                .resultObject(null)
                .status(JobStatus.RECEIVED)
                .errorMsg(null)
                .build();

        Job saved =  jobPort.save(job);

        eventPublisher.novoVideoRecebido(saved);

        return saved;
    }
}
