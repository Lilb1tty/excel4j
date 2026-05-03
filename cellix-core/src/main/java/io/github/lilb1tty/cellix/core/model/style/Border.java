package io.github.lilb1tty.cellix.core.model.style;

public record Border(BorderStyle top, BorderStyle bottom, BorderStyle left, BorderStyle right) {

    public enum BorderStyle { NONE, THIN, MEDIUM, THICK, DASHED, DOTTED }

    public static final Border NONE = new Border(
        BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE);

    public static Border thin() {
        return new Border(BorderStyle.THIN, BorderStyle.THIN, BorderStyle.THIN, BorderStyle.THIN);
    }

    public static Border medium() {
        return new Border(BorderStyle.MEDIUM, BorderStyle.MEDIUM, BorderStyle.MEDIUM, BorderStyle.MEDIUM);
    }
}
