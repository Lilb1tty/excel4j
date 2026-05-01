package io.excel4j.core.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class CellRefTest {

    @Test
    void parseA1Notation() {
        CellRef ref = CellRef.of("A1");
        assertThat(ref.row()).isEqualTo(1);
        assertThat(ref.col()).isEqualTo(1);
    }

    @Test
    void parseZ1() {
        CellRef ref = CellRef.of("Z1");
        assertThat(ref.col()).isEqualTo(26);
    }

    @Test
    void parseAA1() {
        CellRef ref = CellRef.of("AA1");
        assertThat(ref.col()).isEqualTo(27);
    }

    @Test
    void toA1Notation() {
        assertThat(new CellRef(1, 1).toA1()).isEqualTo("A1");
        assertThat(new CellRef(1, 26).toA1()).isEqualTo("Z1");
        assertThat(new CellRef(1, 27).toA1()).isEqualTo("AA1");
    }

    @Test
    void rejectZeroRow() {
        assertThatThrownBy(() -> new CellRef(0, 1))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectZeroCol() {
        assertThatThrownBy(() -> new CellRef(1, 0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rangeContains() {
        CellRange range = new CellRange(CellRef.of("A1"), CellRef.of("C3"));
        assertThat(range.contains(CellRef.of("B2"))).isTrue();
        assertThat(range.contains(CellRef.of("D1"))).isFalse();
    }

    @Test
    void worksheetNameValidation() {
        assertThatThrownBy(() -> new WorksheetName(""))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new WorksheetName("A".repeat(32)))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatCode(() -> new WorksheetName("Sheet1"))
            .doesNotThrowAnyException();
    }
}
