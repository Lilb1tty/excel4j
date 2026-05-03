package io.github.lilb1tty.cellix.core.model;

import io.github.lilb1tty.cellix.core.exception.CellixException;
import io.github.lilb1tty.cellix.core.model.style.CellStyle;
import io.github.lilb1tty.cellix.core.model.style.Font;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.*;

class WorkbookModelTest {

    @Test
    void newWorkbookHasOneDefaultSheet() {
        Workbook wb = new Workbook();
        assertThat(wb.sheets()).hasSize(1);
        assertThat(wb.sheet(1).name().value()).isEqualTo("Sheet1");
    }

    @Test
    void addSheetAndAccessByName() {
        Workbook wb = new Workbook();
        Worksheet orders = wb.addSheet("Orders");
        assertThat(wb.sheet("Orders")).isSameAs(orders);
    }

    @Test
    void accessSheetByIndex() {
        Workbook wb = new Workbook();
        wb.addSheet("Orders");
        assertThat(wb.sheet(2).name().value()).isEqualTo("Orders");
    }

    @Test
    void invalidSheetIndexThrows() {
        Workbook wb = new Workbook();
        assertThatThrownBy(() -> wb.sheet(0)).isInstanceOf(CellixException.class);
        assertThatThrownBy(() -> wb.sheet(2)).isInstanceOf(CellixException.class);
    }

    @Test
    void unknownSheetNameThrows() {
        Workbook wb = new Workbook();
        assertThatThrownBy(() -> wb.sheet("NoSuchSheet"))
            .isInstanceOf(CellixException.class);
    }

    @Test
    void setCellValueByA1() {
        Workbook wb = new Workbook();
        wb.sheet(1).cell("A1").setValue(new TextValue("Hello"));
        assertThat(wb.sheet(1).cell("A1").getValue()).isEqualTo(new TextValue("Hello"));
    }

    @Test
    void setCellValueByRowCol() {
        Workbook wb = new Workbook();
        wb.sheet(1).cell(1, 1).setValue(new NumberValue(42.0));
        assertThat(wb.sheet(1).cell(1, 1).getValue()).isEqualTo(new NumberValue(42.0));
    }

    @Test
    void cellByA1AndRowColReturnSameObject() {
        Workbook wb = new Workbook();
        Cell byA1 = wb.sheet(1).cell("A1");
        Cell byRowCol = wb.sheet(1).cell(1, 1);
        assertThat(byA1).isSameAs(byRowCol);
    }

    @Test
    void unsetCellIsBlank() {
        Workbook wb = new Workbook();
        assertThat(wb.sheet(1).cell("Z99").getValue()).isInstanceOf(BlankValue.class);
    }

    @Test
    void setCellFormula() {
        Workbook wb = new Workbook();
        wb.sheet(1).cell("A3").setFormula("SUM(A1:A2)");
        assertThat(wb.sheet(1).cell("A3").getFormula()).isEqualTo("SUM(A1:A2)");
    }

    @Test
    void setCellStyle() {
        Workbook wb = new Workbook();
        CellStyle bold = CellStyle.DEFAULT.withFont(Font.DEFAULT.bold());
        wb.sheet(1).cell("A1").setStyle(bold);
        assertThat(wb.sheet(1).cell("A1").getStyle()).isEqualTo(bold);
    }

    @Test
    void workbookEmptyHasNoSheets() {
        Workbook wb = Workbook.empty();
        assertThat(wb.sheets()).isEmpty();
    }

    @Test
    void workbookEmptyCanAddSheets() {
        Workbook wb = Workbook.empty();
        wb.addSheet("First");
        assertThat(wb.sheet(1).name().value()).isEqualTo("First");
    }
}
