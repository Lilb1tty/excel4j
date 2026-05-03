package io.github.lilb1tty.excel4j.report;

import io.github.lilb1tty.excel4j.core.model.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class BandExpanderTest {

    @Test
    void findSingleBand() {
        Worksheet sheet = new Worksheet(new WorksheetName("Sheet1"));
        sheet.cell("A2").setValue(new TextValue("<#band items>"));
        sheet.cell("A4").setValue(new TextValue("</band>"));

        var bands = BandExpander.findBands(sheet);
        assertThat(bands).hasSize(1);
        assertThat(bands.get(0).name()).isEqualTo("items");
        assertThat(bands.get(0).startRow()).isEqualTo(2);
        assertThat(bands.get(0).endRow()).isEqualTo(4);
    }

    @Test
    void findNoBands() {
        Worksheet sheet = new Worksheet(new WorksheetName("Sheet1"));
        sheet.cell("A1").setValue(new TextValue("Hello"));

        var bands = BandExpander.findBands(sheet);
        assertThat(bands).isEmpty();
    }

    @Test
    void missingBandEndThrows() {
        Worksheet sheet = new Worksheet(new WorksheetName("Sheet1"));
        sheet.cell("A1").setValue(new TextValue("<#band items>"));

        assertThatThrownBy(() -> BandExpander.findBands(sheet))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Unclosed band");
    }
}
