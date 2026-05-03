package io.github.lilb1tty.cellix.core.exception;

public class CellixException extends RuntimeException {
    public CellixException(String message) {
        super(message);
    }

    public CellixException(String message, Throwable cause) {
        super(message, cause);
    }
}
