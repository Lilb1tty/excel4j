package io.github.lilb1tty.excel4j.core.io;

import io.github.lilb1tty.excel4j.core.io.internal.StyleTable;
import io.github.lilb1tty.excel4j.core.model.style.*;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class StyleTableTest {

    @Test
    void firstAddedStyleGetsIndexZero() {
        StyleTable table = new StyleTable();
        int idx = table.add(CellStyle.DEFAULT);
        assertThat(idx).isEqualTo(0);
    }

    @Test
    void deduplicateStyles() {
        StyleTable table = new StyleTable();
        CellStyle bold = CellStyle.DEFAULT.withFont(Font.DEFAULT.bold());
        int idx1 = table.add(bold);
        int idx2 = table.add(bold);
        assertThat(idx1).isEqualTo(idx2);
        assertThat(table.size()).isEqualTo(1);
    }

    @Test
    void differentStylesGetDifferentIndexes() {
        StyleTable table = new StyleTable();
        int idx1 = table.add(CellStyle.DEFAULT);
        CellStyle bold = CellStyle.DEFAULT.withFont(Font.DEFAULT.bold());
        int idx2 = table.add(bold);
        assertThat(idx1).isNotEqualTo(idx2);
        assertThat(table.size()).isEqualTo(2);
    }

    @Test
    void retrieveByIndex() {
        StyleTable table = new StyleTable();
        CellStyle bold = CellStyle.DEFAULT.withFont(Font.DEFAULT.bold());
        int idx = table.add(bold);
        assertThat(table.get(idx)).isEqualTo(bold);
    }

    @Test
    void allReturnsInsertionOrder() {
        StyleTable table = new StyleTable();
        table.add(CellStyle.DEFAULT);
        CellStyle bold = CellStyle.DEFAULT.withFont(Font.DEFAULT.bold());
        table.add(bold);
        assertThat(table.all()).containsExactly(CellStyle.DEFAULT, bold);
    }
}
