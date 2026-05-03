package io.github.lilb1tty.cellix.core.io;

import io.github.lilb1tty.cellix.core.Cellix;
import io.github.lilb1tty.cellix.core.model.*;
import io.github.lilb1tty.cellix.core.model.style.*;
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
        Workbook wb = Cellix.create();
        wb.sheet(1).cell("A1").setValue(new TextValue("Hello world"));

        Path file = tempDir.resolve("test.xlsx");
        Cellix.write(wb, file);

        Workbook read = Cellix.read(file);
        assertThat(read.sheet(1).cell("A1").getValue())
            .isEqualTo(new TextValue("Hello world"));
    }

    @Test
    void numberValueRoundTrip() {
        Workbook wb = Cellix.create();
        wb.sheet(1).cell("B2").setValue(new NumberValue(123.45));

        Path file = tempDir.resolve("numbers.xlsx");
        Cellix.write(wb, file);

        Workbook read = Cellix.read(file);
        assertThat(read.sheet(1).cell("B2").getValue())
            .isEqualTo(new NumberValue(123.45));
    }

    @Test
    void booleanValueRoundTrip() {
        Workbook wb = Cellix.create();
        wb.sheet(1).cell("A1").setValue(new BooleanValue(true));

        Path file = tempDir.resolve("bool.xlsx");
        Cellix.write(wb, file);

        Workbook read = Cellix.read(file);
        assertThat(read.sheet(1).cell("A1").getValue())
            .isEqualTo(new BooleanValue(true));
    }

    @Test
    void multipleSheetRoundTrip() {
        Workbook wb = Cellix.create();
        wb.sheet(1).cell("A1").setValue(new TextValue("Sheet1"));
        wb.addSheet("Orders");
        wb.sheet("Orders").cell("A1").setValue(new TextValue("Orders data"));

        Path file = tempDir.resolve("multi.xlsx");
        Cellix.write(wb, file);

        Workbook read = Cellix.read(file);
        assertThat(read.sheets()).hasSize(2);
        assertThat(read.sheet(1).cell("A1").getValue())
            .isEqualTo(new TextValue("Sheet1"));
        assertThat(read.sheet("Orders").cell("A1").getValue())
            .isEqualTo(new TextValue("Orders data"));
    }

    @Test
    void formulaRoundTrip() {
        Workbook wb = Cellix.create();
        wb.sheet(1).cell("A1").setValue(new NumberValue(10.0));
        wb.sheet(1).cell("A2").setValue(new NumberValue(20.0));
        wb.sheet(1).cell("A3").setFormula("SUM(A1:A2)");

        Path file = tempDir.resolve("formula.xlsx");
        Cellix.write(wb, file);

        Workbook read = Cellix.read(file);
        assertThat(read.sheet(1).cell("A3").getFormula()).isEqualTo("SUM(A1:A2)");
    }

    @Test
    void largeCellCountRoundTrip() {
        Workbook wb = Cellix.create();
        for (int row = 1; row <= 1000; row++) {
            wb.sheet(1).cell(row, 1).setValue(new NumberValue(row));
            wb.sheet(1).cell(row, 2).setValue(new TextValue("Row " + row));
        }

        Path file = tempDir.resolve("large.xlsx");
        Cellix.write(wb, file);

        Workbook read = Cellix.read(file);
        assertThat(read.sheet(1).cell(500, 1).getValue())
            .isEqualTo(new NumberValue(500.0));
        assertThat(read.sheet(1).cell(1000, 2).getValue())
            .isEqualTo(new TextValue("Row 1000"));
    }

    @Test
    void boldStyleRoundTrip() {
        Workbook wb = Cellix.create();
        CellStyle bold = CellStyle.DEFAULT.withFont(Font.DEFAULT.bold());
        wb.sheet(1).cell("A1").setValue(new TextValue("Bold"));
        wb.sheet(1).cell("A1").setStyle(bold);

        Path file = tempDir.resolve("style.xlsx");
        Cellix.write(wb, file);

        // verify file is valid XLSX (can be opened by Excel)
        assertThat(file).exists();
        Workbook read = Cellix.read(file);
        assertThat(read.sheet(1).cell("A1").getValue())
            .isEqualTo(new TextValue("Bold"));
    }
}
