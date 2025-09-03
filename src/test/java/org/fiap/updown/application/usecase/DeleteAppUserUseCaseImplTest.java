package org.fiap.updown.application.usecase;

import org.fiap.updown.application.port.driver.AppUserPersistencePort;
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
class DeleteAppUserUseCaseImplTest {

    @Mock
    private AppUserPersistencePort appUserPersistencePort;

    @InjectMocks
    private DeleteAppUserUseCaseImpl deleteAppUserUseCase;

    @Test
    void deveDeletarUsuario_QuandoUsuarioExistir() {
        UUID userId = UUID.randomUUID();
        when(appUserPersistencePort.existsById(userId)).thenReturn(true);
        doNothing().when(appUserPersistencePort).deleteById(userId);

        deleteAppUserUseCase.execute(userId);

        verify(appUserPersistencePort, times(1)).existsById(userId);
        verify(appUserPersistencePort, times(1)).deleteById(userId);
    }

    @Test
    void deveLancarExcecao_QuandoUsuarioNaoExistir() {
        UUID userId = UUID.randomUUID();
        when(appUserPersistencePort.existsById(userId)).thenReturn(false);

        assertThatThrownBy(() -> deleteAppUserUseCase.execute(userId))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("AppUser não encontrado: " + userId);

        verify(appUserPersistencePort, never()).deleteById(any(UUID.class));
    }
}