package org.fiap.updown.application.usecase;

import org.fiap.updown.application.port.driver.AppUserPersistencePort;
import org.fiap.updown.domain.exception.ConflitoDeDadosException;
import org.fiap.updown.domain.model.AppUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateAppUserUseCaseImplTest {

    @Mock
    private AppUserPersistencePort appUserPersistencePort;

    @InjectMocks
    private CreateAppUserUseCaseImpl createAppUserUseCase;

    @Test
    void deveCriarUsuario_QuandoEmailNaoExistir() {
        String email = "novo.usuario@teste.com";
        AppUser userToCreate = new AppUser();
        userToCreate.setEmail(email);

        AppUser savedUser = new AppUser();
        savedUser.setId(UUID.randomUUID());
        savedUser.setEmail(email);

        when(appUserPersistencePort.existsByEmail(email)).thenReturn(false);
        when(appUserPersistencePort.save(any(AppUser.class))).thenReturn(savedUser);

        AppUser result = createAppUserUseCase.execute(userToCreate);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(savedUser.getId());
        assertThat(result.getEmail()).isEqualTo(email);

        verify(appUserPersistencePort, times(1)).existsByEmail(email);
        verify(appUserPersistencePort, times(1)).save(userToCreate);
    }

    @Test
    void deveLancarExcecao_QuandoEmailJaExistir() {
        String email = "usuario.existente@teste.com";
        AppUser userToCreate = new AppUser();
        userToCreate.setEmail(email);

        when(appUserPersistencePort.existsByEmail(email)).thenReturn(true);

        assertThatThrownBy(() -> createAppUserUseCase.execute(userToCreate))
                .isInstanceOf(ConflitoDeDadosException.class)
                .hasMessage("E-mail já cadastrado: " + email);

        verify(appUserPersistencePort, never()).save(any(AppUser.class));
    }
}