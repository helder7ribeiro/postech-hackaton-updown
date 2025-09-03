package org.fiap.updown.infrastructure.adapter.rest.appuser.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAppUserRequest(
        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        @Size(max = 255, message = "E-mail deve ter no máximo 255 caracteres")
        String email
) {
}

