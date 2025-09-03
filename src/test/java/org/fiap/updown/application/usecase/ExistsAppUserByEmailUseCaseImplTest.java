package org.fiap.updown.application.usecase;

import org.fiap.updown.application.port.driver.AppUserPersistencePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExistsAppUserByEmailUseCaseImplTest {

    @Mock
    private AppUserPersistencePort appUserPersistencePort;

    @InjectMocks
    private ExistsAppUserByEmailUseCaseImpl existsAppUserByEmailUseCase;

    @Test
    void deveRetornarTrue_QuandoEmailExistir() {
        String email = "existente@teste.com";
        when(appUserPersistencePort.existsByEmail(email)).thenReturn(true);

        boolean result = existsAppUserByEmailUseCase.execute(email);

        assertThat(result).isTrue();
    }

    @Test
    void deveRetornarFalse_QuandoEmailNaoExistir() {
        String email = "nao.existente@teste.com";
        when(appUserPersistencePort.existsByEmail(email)).thenReturn(false);

        boolean result = existsAppUserByEmailUseCase.execute(email);

        assertThat(result).isFalse();
    }
}