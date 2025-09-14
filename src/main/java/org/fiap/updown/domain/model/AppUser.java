package org.fiap.updown.domain.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppUser {
    private UUID id;

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    @Size(max = 255, message = "E-mail deve ter no máximo 255 caracteres")
    private String email;

    @NotBlank(message = "Username é obrigatório")
    @Size(max = 100, message = "Username deve ter no máximo 100 caracteres")
    private String username;
}
