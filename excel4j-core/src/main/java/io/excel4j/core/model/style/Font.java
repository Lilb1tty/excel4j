package io.excel4j.core.model.style;

public record Font(String name, double size, boolean isBold, boolean isItalic,
                   boolean isUnderline, String color) {

    public static final Font DEFAULT = new Font("Calibri", 11, false, false, false, "000000");

    public static Font of(String name, double size) {
        return new Font(name, size, false, false, false, "000000");
    }

    /** Returns a copy with bold set to true. */
    public Font bold() {
        return new Font(name, size, true, isItalic, isUnderline, color);
    }

    /** Returns a copy with italic set to true. */
    public Font italic() {
        return new Font(name, size, isBold, true, isUnderline, color);
    }

    /** Returns a copy with underline set to true. */
    public Font underline() {
        return new Font(name, size, isBold, isItalic, true, color);
    }

    /** Returns a copy with the given hex color. */
    public Font color(String hexColor) {
        return new Font(name, size, isBold, isItalic, isUnderline, hexColor);
    }

    /** Returns a copy with the given size. */
    public Font size(double newSize) {
        return new Font(name, newSize, isBold, isItalic, isUnderline, color);
    }
}
