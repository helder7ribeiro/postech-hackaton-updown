package org.fiap.updown.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceção lançada quando os dados fornecidos para uma operação de negócio são inválidos.
 * Exemplo: tentar atualizar uma entidade sem fornecer seu ID.
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class DadosInvalidosException extends NegocioException {

    public DadosInvalidosException(String message) {
        super(message);
    }
}