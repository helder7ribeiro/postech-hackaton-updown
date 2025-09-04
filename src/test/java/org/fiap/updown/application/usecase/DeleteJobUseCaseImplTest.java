package org.fiap.updown.application.usecase;

import org.fiap.updown.application.port.driver.JobPersistencePort;
import org.fiap.updown.domain.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteJobUseCaseImplTest {

    @Mock
    private JobPersistencePort jobPort;

    @InjectMocks
    private DeleteJobUseCaseImpl deleteJobUseCase;

    @Test
    void deveDeletarJob_QuandoJobExistir() {
        UUID jobId = UUID.randomUUID();
        when(jobPort.existsById(jobId)).thenReturn(true);
        doNothing().when(jobPort).deleteById(jobId);

        deleteJobUseCase.execute(jobId);

        verify(jobPort, times(1)).existsById(jobId);
        verify(jobPort, times(1)).deleteById(jobId);
    }

    @Test
    void deveLancarExcecao_QuandoJobNaoExistirAoTentarDeletar() {
        UUID jobId = UUID.randomUUID();
        when(jobPort.existsById(jobId)).thenReturn(false);

        assertThatThrownBy(() -> deleteJobUseCase.execute(jobId))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Job não encontrado: " + jobId);

        verify(jobPort, never()).deleteById(any(UUID.class));
    }
}