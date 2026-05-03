package io.github.lilb1tty.excel4j.core.io;

import io.github.lilb1tty.excel4j.core.io.internal.SharedStringsTable;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class SharedStringsTableTest {

    @Test
    void addAndRetrieve() {
        SharedStringsTable table = new SharedStringsTable();
        int idx = table.add("Hello");
        assertThat(table.get(idx)).isEqualTo("Hello");
    }

    @Test
    void deduplicateStrings() {
        SharedStringsTable table = new SharedStringsTable();
        int idx1 = table.add("Hello");
        int idx2 = table.add("Hello");
        assertThat(idx1).isEqualTo(idx2);
        assertThat(table.size()).isEqualTo(1);
    }

    @Test
    void sequentialIndexes() {
        SharedStringsTable table = new SharedStringsTable();
        int a = table.add("A");
        int b = table.add("B");
        int c = table.add("C");
        assertThat(a).isEqualTo(0);
        assertThat(b).isEqualTo(1);
        assertThat(c).isEqualTo(2);
    }

    @Test
    void allReturnsInsertionOrder() {
        SharedStringsTable table = new SharedStringsTable();
        table.add("X");
        table.add("Y");
        assertThat(table.all()).containsExactly("X", "Y");
    }
}
