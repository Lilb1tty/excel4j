package io.github.lilb1tty.excel4j.core.exception;

public class ExcelWriteException extends ExcelException {
    public ExcelWriteException(String message) {
        super(message);
    }

    public ExcelWriteException(String message, Throwable cause) {
        super(message, cause);
    }
}
