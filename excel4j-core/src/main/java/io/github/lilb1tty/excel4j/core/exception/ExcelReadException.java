package io.github.lilb1tty.excel4j.core.exception;

public class ExcelReadException extends ExcelException {
    public ExcelReadException(String message) {
        super(message);
    }

    public ExcelReadException(String message, Throwable cause) {
        super(message, cause);
    }
}
