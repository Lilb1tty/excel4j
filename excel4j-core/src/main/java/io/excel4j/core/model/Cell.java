package io.excel4j.core.model;

import io.excel4j.core.model.style.CellStyle;

public class Cell {

    private final CellRef ref;
    private CellValue value;
    private String formula;
    private CellStyle style;

    public Cell(CellRef ref) {
        this.ref = ref;
        this.value = new BlankValue();
        this.style = CellStyle.DEFAULT;
    }

    public CellRef getRef()       { return ref; }
    public CellValue getValue()   { return value; }
    public String getFormula()    { return formula; }
    public CellStyle getStyle()   { return style; }

    public void setValue(CellValue value) {
        this.value = value != null ? value : new BlankValue();
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public void setStyle(CellStyle style) {
        this.style = style != null ? style : CellStyle.DEFAULT;
    }
}
