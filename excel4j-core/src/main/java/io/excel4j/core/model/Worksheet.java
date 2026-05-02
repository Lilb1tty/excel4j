package io.excel4j.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Worksheet {

    private final WorksheetName name;
    private final Map<CellRef, Cell> cells = new HashMap<>();
    private final List<Chart> charts = new ArrayList<>();
    private final List<PivotTable> pivotTables = new ArrayList<>();

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

    public void addChart(Chart chart) {
        charts.add(chart);
    }

    public List<Chart> charts() {
        return List.copyOf(charts);
    }

    public void addPivotTable(PivotTable pivotTable) {
        pivotTables.add(pivotTable);
    }

    public List<PivotTable> pivotTables() {
        return List.copyOf(pivotTables);
    }
}
