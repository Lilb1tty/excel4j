package io.excel4j.core.model;

import java.util.HashMap;
import java.util.Map;

public class Worksheet {

    private final WorksheetName name;
    private final Map<CellRef, Cell> cells = new HashMap<>();

    public Worksheet(WorksheetName name) {
        this.name = name;
    }

    public WorksheetName name() { return name; }

    public Cell cell(int row, int col) {
        return cells.computeIfAbsent(new CellRef(row, col), Cell::new);
    }

    public Cell cell(String a1) {
        return cells.computeIfAbsent(CellRef.of(a1), Cell::new);
    }

    public Cell cell(CellRef ref) {
        return cells.computeIfAbsent(ref, Cell::new);
    }

    public Map<CellRef, Cell> cells() {
        return Map.copyOf(cells);
    }
}
