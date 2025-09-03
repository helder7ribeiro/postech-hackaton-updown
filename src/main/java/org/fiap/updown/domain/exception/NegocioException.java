package org.fiap.updown.domain.exception;

/**
 * Classe base para todas as exceções de negócio da aplicação.
 * Herdar de RuntimeException nos permite usar exceções não checadas
 */
public abstract class NegocioException extends RuntimeException {
    protected NegocioException(String message) {
        super(message);
    }

    protected NegocioException(String message, Throwable cause) {
        super(message, cause);
    }
}
