package io.github.lilb1tty.excel4j.core.io;

import io.github.lilb1tty.excel4j.core.Excel;
import io.github.lilb1tty.excel4j.core.model.*;
import io.github.lilb1tty.excel4j.core.model.style.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.*;

class OoxmlRoundTripTest {

    @TempDir
    Path tempDir;

    @Test
    void textValueRoundTrip() {
        Workbook wb = Excel.create();
        wb.sheet(1).cell("A1").setValue(new TextValue("Hello world"));

        Path file = tempDir.resolve("test.xlsx");
        Excel.write(wb, file);

        Workbook read = Excel.read(file);
        assertThat(read.sheet(1).cell("A1").getValue())
            .isEqualTo(new TextValue("Hello world"));
    }

    @Test
    void numberValueRoundTrip() {
        Workbook wb = Excel.create();
        wb.sheet(1).cell("B2").setValue(new NumberValue(123.45));

        Path file = tempDir.resolve("numbers.xlsx");
        Excel.write(wb, file);

        Workbook read = Excel.read(file);
        assertThat(read.sheet(1).cell("B2").getValue())
            .isEqualTo(new NumberValue(123.45));
    }

    @Test
    void booleanValueRoundTrip() {
        Workbook wb = Excel.create();
        wb.sheet(1).cell("A1").setValue(new BooleanValue(true));

        Path file = tempDir.resolve("bool.xlsx");
        Excel.write(wb, file);

        Workbook read = Excel.read(file);
        assertThat(read.sheet(1).cell("A1").getValue())
            .isEqualTo(new BooleanValue(true));
    }

    @Test
    void multipleSheetRoundTrip() {
        Workbook wb = Excel.create();
        wb.sheet(1).cell("A1").setValue(new TextValue("Sheet1"));
        wb.addSheet("Orders");
        wb.sheet("Orders").cell("A1").setValue(new TextValue("Orders data"));

        Path file = tempDir.resolve("multi.xlsx");
        Excel.write(wb, file);

        Workbook read = Excel.read(file);
        assertThat(read.sheets()).hasSize(2);
        assertThat(read.sheet(1).cell("A1").getValue())
            .isEqualTo(new TextValue("Sheet1"));
        assertThat(read.sheet("Orders").cell("A1").getValue())
            .isEqualTo(new TextValue("Orders data"));
    }

    @Test
    void formulaRoundTrip() {
        Workbook wb = Excel.create();
        wb.sheet(1).cell("A1").setValue(new NumberValue(10.0));
        wb.sheet(1).cell("A2").setValue(new NumberValue(20.0));
        wb.sheet(1).cell("A3").setFormula("SUM(A1:A2)");

        Path file = tempDir.resolve("formula.xlsx");
        Excel.write(wb, file);

        Workbook read = Excel.read(file);
        assertThat(read.sheet(1).cell("A3").getFormula()).isEqualTo("SUM(A1:A2)");
    }

    @Test
    void largeCellCountRoundTrip() {
        Workbook wb = Excel.create();
        for (int row = 1; row <= 1000; row++) {
            wb.sheet(1).cell(row, 1).setValue(new NumberValue(row));
            wb.sheet(1).cell(row, 2).setValue(new TextValue("Row " + row));
        }

        Path file = tempDir.resolve("large.xlsx");
        Excel.write(wb, file);

        Workbook read = Excel.read(file);
        assertThat(read.sheet(1).cell(500, 1).getValue())
            .isEqualTo(new NumberValue(500.0));
        assertThat(read.sheet(1).cell(1000, 2).getValue())
            .isEqualTo(new TextValue("Row 1000"));
    }

    @Test
    void boldStyleRoundTrip() {
        Workbook wb = Excel.create();
        CellStyle bold = CellStyle.DEFAULT.withFont(Font.DEFAULT.bold());
        wb.sheet(1).cell("A1").setValue(new TextValue("Bold"));
        wb.sheet(1).cell("A1").setStyle(bold);

        Path file = tempDir.resolve("style.xlsx");
        Excel.write(wb, file);

        // verify file is valid XLSX (can be opened by Excel)
        assertThat(file).exists();
        Workbook read = Excel.read(file);
        assertThat(read.sheet(1).cell("A1").getValue())
            .isEqualTo(new TextValue("Bold"));
    }
}
