package org.fiap.updown.application.usecase;

import org.fiap.updown.application.port.driver.AppUserPersistencePort;
import org.fiap.updown.application.port.driver.EventPublisher;
import org.fiap.updown.application.port.driver.JobPersistencePort;
import org.fiap.updown.application.port.driver.VideoStorage;
import org.fiap.updown.domain.CreateJobCommand;
import org.fiap.updown.domain.exception.FalhaInfraestruturaException;
import org.fiap.updown.domain.exception.RecursoNaoEncontradoException;
import org.fiap.updown.domain.model.AppUser;
import org.fiap.updown.domain.model.Job;
import org.fiap.updown.domain.service.JobService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateJobUseCaseImplTest {

    @Mock
    private AppUserPersistencePort appUserPort;
    @Mock
    private JobPersistencePort jobPort;
    @Mock
    private EventPublisher eventPublisher;
    @Mock
    private VideoStorage videoStorage;
    @Mock
    private JobService jobService;

    @InjectMocks
    private CreateJobUseCaseImpl createJobUseCase;

    @Test
    void deveCriarJobComSucesso() {
        UUID userId = UUID.randomUUID();
        InputStream videoData = new ByteArrayInputStream("video data".getBytes());
        CreateJobCommand command = new CreateJobCommand(userId, "video.mp4", "video/mp4", videoData);

        AppUser owner = new AppUser();
        owner.setId(userId);
        owner.setEmail("dono.job@teste.com");

        String s3Path = "s3://videos/input/some-path/video.mp4";

        Job jobCriadoPeloServico = Job.builder().user(owner).sourceObject(s3Path).build();
        Job jobSalvo = Job.builder().id(UUID.randomUUID()).user(owner).sourceObject(s3Path).build();

        when(appUserPort.findById(userId)).thenReturn(Optional.of(owner));
        when(videoStorage.store(userId, "video.mp4", "video/mp4", videoData)).thenReturn(s3Path);
        when(jobService.createJob(owner, s3Path)).thenReturn(jobCriadoPeloServico);
        when(jobPort.save(jobCriadoPeloServico)).thenReturn(jobSalvo);
        doNothing().when(eventPublisher).novoVideoRecebido(jobSalvo);

        Job result = createJobUseCase.execute(command);


        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(jobSalvo.getId());
        assertThat(result.getSourceObject()).isEqualTo(s3Path);
        assertThat(result.getUser()).isEqualTo(owner);

        verify(appUserPort, times(1)).findById(userId);
        verify(videoStorage, times(1)).store(userId, "video.mp4", "video/mp4", videoData);
        verify(jobService, times(1)).createJob(owner, s3Path);
        verify(jobPort, times(1)).save(jobCriadoPeloServico);
        verify(eventPublisher, times(1)).novoVideoRecebido(jobSalvo);
    }

    @Test
    void deveLancarExcecao_QuandoUsuarioNaoForEncontrado() {
        UUID userId = UUID.randomUUID();
        CreateJobCommand command = new CreateJobCommand(userId, "video.mp4", "video/mp4", null);

        when(appUserPort.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> createJobUseCase.execute(command))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Usuário não encontrado: " + userId);

        verify(videoStorage, never()).store(any(), anyString(), anyString(), any());
        verify(jobPort, never()).save(any());
    }

    @Test
    void deveLancarExcecao_QuandoFalharAoArmazenarVideo() {
        UUID userId = UUID.randomUUID();
        InputStream videoData = new ByteArrayInputStream("video data".getBytes());
        CreateJobCommand command = new CreateJobCommand(userId, "video.mp4", "video/mp4", videoData);

        AppUser owner = new AppUser();
        owner.setId(userId);

        when(appUserPort.findById(userId)).thenReturn(Optional.of(owner));
        when(videoStorage.store(userId, "video.mp4", "video/mp4", videoData))
                .thenThrow(new RuntimeException("Erro no S3"));

        assertThatThrownBy(() -> createJobUseCase.execute(command))
                .isInstanceOf(FalhaInfraestruturaException.class)
                .hasMessage("Falha ao armazenar vídeo: Erro no S3");

        verify(jobPort, never()).save(any());
        verify(eventPublisher, never()).novoVideoRecebido(any());
    }
}