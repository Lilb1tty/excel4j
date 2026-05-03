package io.github.lilb1tty.excel4j.core.model;

import java.util.Map;

public record Row(int rowNum, Map<Integer, CellValue> cells) {

    public Row {
        cells = Map.copyOf(cells);
    }

    private static final BlankValue BLANK = new BlankValue();

    public CellValue cell(int col) {
        return cells.getOrDefault(col, BLANK);
    }
}
