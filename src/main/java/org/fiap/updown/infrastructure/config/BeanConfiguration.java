package org.fiap.updown.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.fiap.updown.application.port.driver.AppUserPersistencePort;
import org.fiap.updown.application.port.driver.EventPublisher;
import org.fiap.updown.application.port.driver.JobPersistencePort;
import org.fiap.updown.application.port.driver.VideoStorage;
import org.fiap.updown.application.usecase.*;
import org.fiap.updown.application.port.driven.GetAppUserByUsernameUseCase;
import org.fiap.updown.domain.service.JobService;
import org.fiap.updown.domain.service.JobServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    // Domain Services
    @Bean
    public JobService jobService() {
        return new JobServiceImpl();
    }

    // Use Cases - AppUser
    @Bean
    public CreateAppUserUseCaseImpl createAppUserUseCase(AppUserPersistencePort appUserPersistencePort) {
        return new CreateAppUserUseCaseImpl(appUserPersistencePort);
    }

    @Bean
    public GetAppUserByIdUseCaseImpl getAppUserByIdUseCase(AppUserPersistencePort appUserPersistencePort) {
        return new GetAppUserByIdUseCaseImpl(appUserPersistencePort);
    }

    @Bean
    public UpdateAppUserUseCaseImpl updateAppUserUseCase(AppUserPersistencePort appUserPersistencePort) {
        return new UpdateAppUserUseCaseImpl(appUserPersistencePort);
    }

    @Bean
    public DeleteAppUserUseCaseImpl deleteAppUserUseCase(AppUserPersistencePort appUserPersistencePort) {
        return new DeleteAppUserUseCaseImpl(appUserPersistencePort);
    }

    @Bean
    public ExistsAppUserByEmailUseCaseImpl existsAppUserByEmailUseCase(AppUserPersistencePort appUserPersistencePort) {
        return new ExistsAppUserByEmailUseCaseImpl(appUserPersistencePort);
    }

    @Bean
    public GetAppUserByUsernameUseCaseImpl getAppUserByUsernameUseCase(AppUserPersistencePort appUserPersistencePort) {
        return new GetAppUserByUsernameUseCaseImpl(appUserPersistencePort);
    }

    @Bean
    public CreateJobUseCaseImpl createJobUseCase(
            GetAppUserByUsernameUseCase getAppUserByUsernameUseCase,
            JobPersistencePort jobPort,
            EventPublisher eventPublisher,
            VideoStorage videoStorage,
            JobService jobService) {
        return new CreateJobUseCaseImpl(getAppUserByUsernameUseCase, jobPort, eventPublisher, videoStorage, jobService);
    }

    @Bean
    public GetJobByIdUseCaseImpl getJobByIdUseCase(JobPersistencePort jobPersistencePort) {
        return new GetJobByIdUseCaseImpl(jobPersistencePort);
    }

    @Bean
    public UpdateJobUseCaseImpl updateJobUseCase(JobPersistencePort jobPersistencePort) {
        return new UpdateJobUseCaseImpl(jobPersistencePort);
    }

    @Bean
    public DeleteJobUseCaseImpl deleteJobUseCase(JobPersistencePort jobPersistencePort) {
        return new DeleteJobUseCaseImpl(jobPersistencePort);
    }

    @Bean
    public ExistsJobByIdUseCaseImpl existsJobByIdUseCase(JobPersistencePort jobPersistencePort) {
        return new ExistsJobByIdUseCaseImpl(jobPersistencePort);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}