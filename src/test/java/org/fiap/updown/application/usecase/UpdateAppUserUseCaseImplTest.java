package org.fiap.updown.application.usecase;

import org.fiap.updown.application.port.driver.AppUserPersistencePort;
import org.fiap.updown.domain.exception.RecursoNaoEncontradoException;
import org.fiap.updown.domain.model.AppUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateAppUserUseCaseImplTest {

    @Mock
    private AppUserPersistencePort appUserPersistencePort;

    @InjectMocks
    private UpdateAppUserUseCaseImpl updateAppUserUseCase;

    @Test
    void deveAtualizarUsuario_QuandoUsuarioExistir() {
        UUID userId = UUID.randomUUID();
        String novoEmail = "email.atualizado@teste.com";

        AppUser userParaAtualizar = new AppUser();
        userParaAtualizar.setId(userId);
        userParaAtualizar.setEmail(novoEmail);

        AppUser usuarioExistente = new AppUser();
        usuarioExistente.setId(userId);
        usuarioExistente.setEmail("email.antigo@teste.com");

        when(appUserPersistencePort.findById(userId)).thenReturn(Optional.of(usuarioExistente));
        when(appUserPersistencePort.save(any(AppUser.class))).thenReturn(userParaAtualizar);

        AppUser result = updateAppUserUseCase.execute(userParaAtualizar);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getEmail()).isEqualTo(novoEmail);

        verify(appUserPersistencePort, times(1)).findById(userId);
        verify(appUserPersistencePort, times(1)).save(usuarioExistente);
        assertThat(usuarioExistente.getEmail()).isEqualTo(novoEmail);
    }

    @Test
    void deveLancarExcecao_QuandoUsuarioNaoExistir() {
        UUID userId = UUID.randomUUID();
        AppUser userParaAtualizar = new AppUser();
        userParaAtualizar.setId(userId);
        userParaAtualizar.setEmail("email.qualquer@teste.com");

        when(appUserPersistencePort.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> updateAppUserUseCase.execute(userParaAtualizar))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("AppUser não encontrado: " + userId);

        verify(appUserPersistencePort, never()).save(any(AppUser.class));
    }
}