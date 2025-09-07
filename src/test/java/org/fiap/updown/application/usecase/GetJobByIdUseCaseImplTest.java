package org.fiap.updown.application.usecase;

import org.fiap.updown.application.port.driver.JobPersistencePort;
import org.fiap.updown.domain.exception.RecursoNaoEncontradoException;
import org.fiap.updown.domain.model.Job;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetJobByIdUseCaseImplTest {

    @Mock
    private JobPersistencePort jobPort;

    @InjectMocks
    private GetJobByIdUseCaseImpl getJobByIdUseCase;

    @Test
    void deveRetornarJob_QuandoEncontrado() {
        UUID jobId = UUID.randomUUID();
        Job jobExistente = new Job();
        jobExistente.setId(jobId);

        when(jobPort.findById(jobId)).thenReturn(Optional.of(jobExistente));

        Job result = getJobByIdUseCase.execute(jobId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(jobId);
    }

    @Test
    void deveLancarExcecao_QuandoNaoEncontrado() {
        UUID jobId = UUID.randomUUID();
        when(jobPort.findById(jobId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getJobByIdUseCase.execute(jobId))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Job não encontrado: " + jobId);
    }
}