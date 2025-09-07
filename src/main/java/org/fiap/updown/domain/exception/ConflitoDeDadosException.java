package org.fiap.updown.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Lançada quando uma operação viola uma regra de negócio, como a tentativa
 * de criar um recurso que já existe (ex: e-mail duplicado).
 */
@ResponseStatus(value = HttpStatus.CONFLICT)
public class ConflitoDeDadosException extends NegocioException {
    public ConflitoDeDadosException(String message) {
        super(message);
    }
}
