package org.fiap.updown.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Lançada quando um recurso esperado não é encontrado.
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class RecursoNaoEncontradoException extends NegocioException {
    public RecursoNaoEncontradoException(String message) {
        super(message);
    }
}
