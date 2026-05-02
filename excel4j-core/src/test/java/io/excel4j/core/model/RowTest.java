package io.excel4j.core.model;

import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.assertj.core.api.Assertions.*;

class RowTest {

    @Test
    void rowNumAndCellAccess() {
        var cells = Map.of(1, (CellValue) new TextValue("A"), 2, (CellValue) new NumberValue(42.0));
        var row = new Row(3, cells);
        assertThat(row.rowNum()).isEqualTo(3);
        assertThat(row.cell(1)).isEqualTo(new TextValue("A"));
        assertThat(row.cell(2)).isEqualTo(new NumberValue(42.0));
    }

    @Test
    void missingCellReturnsBlank() {
        var row = new Row(1, Map.of());
        assertThat(row.cell(99)).isInstanceOf(BlankValue.class);
    }

    @Test
    void cellsReturnsDefensiveCopy() {
        var row = new Row(1, Map.of(1, (CellValue) new TextValue("x")));
        assertThat(row.cells()).containsKey(1);
    }
}
