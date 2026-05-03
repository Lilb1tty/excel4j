package io.github.lilb1tty.cellix.core.model;

import io.github.lilb1tty.cellix.core.model.style.CellStyle;
import java.util.List;

public final class RangeSelection {

    private final Worksheet sheet;
    private final CellRange range;

    RangeSelection(Worksheet sheet, CellRange range) {
        this.sheet = sheet;
        this.range = range;
    }

    public RangeSelection setStyle(CellStyle style) {
        for (int row = range.first().row(); row <= range.last().row(); row++) {
            for (int col = range.first().col(); col <= range.last().col(); col++) {
                sheet.cell(row, col).setStyle(style);
            }
        }
        return this;
    }

    /** Sets values row by row. Each inner list is one row of values. */
    public RangeSelection setValues(List<? extends List<? extends CellValue>> values) {
        for (int r = 0; r < values.size(); r++) {
            List<? extends CellValue> rowValues = values.get(r);
            for (int c = 0; c < rowValues.size(); c++) {
                int absRow = range.first().row() + r;
                int absCol = range.first().col() + c;
                if (absRow <= range.last().row() && absCol <= range.last().col()) {
                    sheet.cell(absRow, absCol).setValue(rowValues.get(c));
                }
            }
        }
        return this;
    }

    public CellRange range() {
        return range;
    }
}
