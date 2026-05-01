package io.excel4j.core.model.style;

public record CellStyle(Font font, Fill fill, Border border, NumberFormat numberFormat) {

    public static final CellStyle DEFAULT = new CellStyle(
        Font.DEFAULT, Fill.NONE, Border.NONE, NumberFormat.GENERAL);

    public CellStyle withFont(Font f)                 { return new CellStyle(f, fill, border, numberFormat); }
    public CellStyle withFill(Fill f)                 { return new CellStyle(font, f, border, numberFormat); }
    public CellStyle withBorder(Border b)             { return new CellStyle(font, fill, b, numberFormat); }
    public CellStyle withNumberFormat(NumberFormat n) { return new CellStyle(font, fill, border, n); }
}
