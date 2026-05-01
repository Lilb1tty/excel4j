package io.excel4j.core.model;

public enum ErrorType {
    DIV_BY_ZERO,
    VALUE,
    REF,
    NAME,
    NA,
    NULL,
    NUM,
    CIRCULAR_REF;

    public String toExcelString() {
        return switch (this) {
            case DIV_BY_ZERO -> "#DIV/0!";
            case VALUE -> "#VALUE!";
            case REF -> "#REF!";
            case NAME -> "#NAME?";
            case NA -> "#N/A";
            case NULL -> "#NULL!";
            case NUM -> "#NUM!";
            case CIRCULAR_REF -> "#CIRCULAR_REF!";
        };
    }

    public static ErrorType fromExcelString(String s) {
        return switch (s) {
            case "#DIV/0!" -> DIV_BY_ZERO;
            case "#VALUE!" -> VALUE;
            case "#REF!" -> REF;
            case "#NAME?" -> NAME;
            case "#N/A" -> NA;
            case "#NULL!" -> NULL;
            case "#NUM!" -> NUM;
            default -> throw new IllegalArgumentException("Unknown Excel error: " + s);
        };
    }
}
