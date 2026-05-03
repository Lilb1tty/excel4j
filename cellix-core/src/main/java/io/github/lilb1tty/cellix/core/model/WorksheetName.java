package io.github.lilb1tty.cellix.core.model;

public record WorksheetName(String value) {

    public WorksheetName {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Worksheet name must not be blank");
        if (value.length() > 31)
            throw new IllegalArgumentException(
                "Worksheet name must be <= 31 chars, got: " + value.length());
    }
}
