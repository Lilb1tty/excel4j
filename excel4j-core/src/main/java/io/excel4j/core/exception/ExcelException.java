package io.excel4j.core.exception;

public class ExcelException extends RuntimeException {
    public ExcelException(String message) {
        super(message);
    }

    public ExcelException(String message, Throwable cause) {
        super(message, cause);
    }
}
