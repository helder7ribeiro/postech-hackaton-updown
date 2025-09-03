package org.fiap.updown.application.usecase;

import org.fiap.updown.application.port.driver.JobPersistencePort;
import org.fiap.updown.domain.exception.DadosInvalidosException;
import org.fiap.updown.domain.exception.RecursoNaoEncontradoException;
import org.fiap.updown.domain.model.Job;
import org.fiap.updown.domain.model.JobStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateJobUseCaseImplTest {

    @Mock
    private JobPersistencePort jobPort;

    @InjectMocks
    private UpdateJobUseCaseImpl updateJobUseCase;

    @Test
    void deveAtualizarJob_QuandoJobExistir() {
        UUID jobId = UUID.randomUUID();
        Job patch = new Job();
        patch.setId(jobId);
        patch.setStatus(JobStatus.COMPLETED);
        patch.setResultObject("s3://videos/output/result.mp4");

        Job jobExistente = new Job();
        jobExistente.setId(jobId);
        jobExistente.setStatus(JobStatus.PROCESSING);

        when(jobPort.findById(jobId)).thenReturn(Optional.of(jobExistente));
        when(jobPort.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Job result = updateJobUseCase.execute(patch);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(jobId);
        assertThat(result.getStatus()).isEqualTo(JobStatus.COMPLETED);
        assertThat(result.getResultObject()).isEqualTo("s3://videos/output/result.mp4");

        verify(jobPort, times(1)).findById(jobId);
        verify(jobPort, times(1)).save(jobExistente);
    }

    @Test
    void deveLancarExcecao_QuandoJobNaoExistir() {
        UUID jobId = UUID.randomUUID();
        Job patch = new Job();
        patch.setId(jobId);
        patch.setStatus(JobStatus.FAILED);

        when(jobPort.findById(jobId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> updateJobUseCase.execute(patch))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Job não encontrado: " + jobId);

        verify(jobPort, never()).save(any());
    }

    @Test
    void deveLancarExcecao_QuandoIdDoJobForNuloNoPatch() {
        Job patchSemId = new Job();
        patchSemId.setId(null);

        assertThatThrownBy(() -> updateJobUseCase.execute(patchSemId))
                .isInstanceOf(DadosInvalidosException.class)
                .hasMessage("ID do Job não pode ser nulo.");

        verify(jobPort, never()).findById(any());
        verify(jobPort, never()).save(any());
    }
}