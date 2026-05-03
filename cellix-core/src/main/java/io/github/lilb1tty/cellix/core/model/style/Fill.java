package io.github.lilb1tty.cellix.core.model.style;

public record Fill(FillPattern pattern, String foregroundColor, String backgroundColor) {

    public enum FillPattern { NONE, SOLID }

    public static final Fill NONE = new Fill(FillPattern.NONE, "FFFFFF", "FFFFFF");

    public static Fill solid(String hexColor) {
        return new Fill(FillPattern.SOLID, hexColor, "FFFFFF");
    }
}
