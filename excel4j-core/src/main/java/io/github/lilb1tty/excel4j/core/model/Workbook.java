package io.github.lilb1tty.excel4j.core.model;

import io.github.lilb1tty.excel4j.core.exception.ExcelException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class Workbook {

    private final List<Worksheet> sheets = new ArrayList<>();

    public Workbook() {
        sheets.add(new Worksheet(new WorksheetName("Sheet1")));
    }

    private Workbook(boolean empty) {}

    public static Workbook empty() {
        return new Workbook(true);
    }

    public Worksheet addSheet(String name) {
        Worksheet sheet = new Worksheet(new WorksheetName(name));
        sheets.add(sheet);
        return sheet;
    }

    public Worksheet sheet(int index) {
        if (index < 1 || index > sheets.size())
            throw new ExcelException("Sheet index out of range: " + index
                + " (workbook has " + sheets.size() + " sheets)");
        return sheets.get(index - 1);
    }

    public Worksheet sheet(String name) {
        return sheets.stream()
            .filter(s -> s.name().value().equals(name))
            .findFirst()
            .orElseThrow(() -> new ExcelException("Sheet not found: " + name));
    }

    public List<Worksheet> sheets() {
        return List.copyOf(sheets);
    }

    public void recalculate(BiFunction<Worksheet, Cell, CellValue> evaluator) {
        for (Worksheet sheet : sheets) {
            for (Cell cell : sheet.cells().values()) {
                if (cell.getFormula() != null) {
                    CellValue result = evaluator.apply(sheet, cell);
                    cell.setCachedValue(result);
                }
            }
        }
    }
}
