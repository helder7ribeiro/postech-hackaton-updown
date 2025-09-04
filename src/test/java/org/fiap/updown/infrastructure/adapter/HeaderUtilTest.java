package org.fiap.updown.infrastructure.adapter;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import static org.assertj.core.api.Assertions.assertThat;

class HeaderUtilTest {

    private static final String APPLICATION_NAME = "updown";

    @Test
    void shouldCreateAlertHeaders() {
        String message = "Teste de Alerta";
        String param = "ParamTeste";

        HttpHeaders headers = HeaderUtil.createAlert(APPLICATION_NAME, message, param);

        assertThat(headers).isNotNull();
        assertThat(headers.getFirst("X-updown-alert")).isEqualTo(message);
        assertThat(headers.getFirst("X-updown-params")).isEqualTo(param);
    }

    @Test
    void shouldCreateFailureAlertHeaders() {
        String entityName = "AppUser";
        String errorKey = "userNotFound";
        String defaultMessage = "Usuário não encontrado";

        HttpHeaders headers = HeaderUtil.createFailureAlert(APPLICATION_NAME, true, entityName, errorKey, defaultMessage);

        assertThat(headers).isNotNull();
        assertThat(headers.getFirst("X-updown-error")).isEqualTo("error." + errorKey);
        assertThat(headers.getFirst("X-updown-params")).isEqualTo(entityName);
    }
}