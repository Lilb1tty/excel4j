package io.github.lilb1tty.excel4j.core.model.style;

public record NumberFormat(String formatCode) {

    public static final NumberFormat GENERAL    = new NumberFormat("General");
    public static final NumberFormat INTEGER    = new NumberFormat("0");
    public static final NumberFormat DECIMAL_2  = new NumberFormat("#,##0.00");
    public static final NumberFormat PERCENTAGE = new NumberFormat("0.00%");
    public static final NumberFormat DATE_SHORT = new NumberFormat("yyyy-mm-dd");
    public static final NumberFormat DATE_LONG  = new NumberFormat("d mmmm yyyy");
    public static final NumberFormat DATETIME   = new NumberFormat("yyyy-mm-dd hh:mm:ss");
    public static final NumberFormat CURRENCY   = new NumberFormat("\"$\"#,##0.00");

    public static NumberFormat custom(String code) {
        return new NumberFormat(code);
    }

    public boolean isDateFormat() {
        String lc = formatCode.toLowerCase();
        return lc.contains("y") || lc.contains("d") || lc.contains("h")
            || lc.contains("m") || lc.contains("s");
    }
}
