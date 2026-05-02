package io.excel4j.core.io;

import io.excel4j.core.Excel;
import io.excel4j.core.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class SheetStreamReaderTest {

    @TempDir Path tmp;

    private Path sampleFile() {
        Path p = tmp.resolve("sample.xlsx");
        var wb = Excel.create();
        var sheet = wb.sheet(1);
        sheet.cell("A1").setValue(new TextValue("Name"));
        sheet.cell("B1").setValue(new TextValue("Score"));
        sheet.cell("A2").setValue(new TextValue("Alice"));
        sheet.cell("B2").setValue(new NumberValue(95.0));
        sheet.cell("A3").setValue(new TextValue("Bob"));
        sheet.cell("B3").setValue(new NumberValue(82.0));
        Excel.write(wb, p);
        return p;
    }

    @Test
    void streamRowsReturnAllRows() {
        List<Row> rows = new ArrayList<>();
        try (var reader = Excel.streamReader(sampleFile(), 1)) {
            reader.stream().forEach(rows::add);
        }
        assertThat(rows).hasSize(3);
        assertThat(rows.get(0).cell(1)).isEqualTo(new TextValue("Name"));
        assertThat(rows.get(1).cell(1)).isEqualTo(new TextValue("Alice"));
        assertThat(rows.get(2).cell(1)).isEqualTo(new TextValue("Bob"));
    }

    @Test
    void streamRowsNumberValues() {
        List<Row> rows = new ArrayList<>();
        try (var reader = Excel.streamReader(sampleFile(), 1)) {
            reader.stream().forEach(rows::add);
        }
        assertThat(rows.get(1).cell(2)).isEqualTo(new NumberValue(95.0));
        assertThat(rows.get(2).cell(2)).isEqualTo(new NumberValue(82.0));
    }

    @Test
    void forEachConvenienceMethod() {
        List<Integer> rowNums = new ArrayList<>();
        try (var reader = Excel.streamReader(sampleFile(), 1)) {
            reader.forEach(row -> rowNums.add(row.rowNum()));
        }
        assertThat(rowNums).containsExactly(1, 2, 3);
    }

    @Test
    void streamWithFilterAndCollect() {
        List<Row> rows;
        try (var reader = Excel.streamReader(sampleFile(), 1)) {
            rows = reader.stream()
                .filter(r -> r.rowNum() > 1)
                .toList();
        }
        assertThat(rows).hasSize(2);
    }

    @Test
    void missingCellReturnsBlank() {
        try (var reader = Excel.streamReader(sampleFile(), 1)) {
            reader.stream().findFirst().ifPresent(row ->
                assertThat(row.cell(99)).isInstanceOf(BlankValue.class)
            );
        }
    }
}
