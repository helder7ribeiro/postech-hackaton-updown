package org.fiap.updown.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Lançada quando ocorre um erro na comunicação com um serviço externo,
 * como S3, SQS, ou um banco de dados que está temporariamente indisponível.
 */
@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class FalhaInfraestruturaException extends NegocioException {
    public FalhaInfraestruturaException(String message, Throwable cause) {
        super(message, cause);
    }
}
