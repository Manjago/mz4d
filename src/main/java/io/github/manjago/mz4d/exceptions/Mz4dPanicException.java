package io.github.manjago.mz4d.exceptions;

/**
 * Если это исключение произошло - безусловно прерываем работу
 */
public class Mz4dPanicException extends RuntimeException {
    public Mz4dPanicException(String message, Throwable cause) {
        super(message, cause);
    }
}
