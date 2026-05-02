package io.excel4j.core.model;

import java.util.Map;

public record Row(int rowNum, Map<Integer, CellValue> cells) {

    public Row {
        cells = Map.copyOf(cells);
    }

    public CellValue cell(int col) {
        return cells.getOrDefault(col, new BlankValue());
    }
}
