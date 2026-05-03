package io.github.lilb1tty.cellix.report;

import io.github.lilb1tty.cellix.core.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class ReportEngineTest {

    @Test
    void substituteSingleValue() {
        Workbook wb = Workbook.empty();
        wb.addSheet("Sheet1").cell("A1").setValue(new TextValue("<#value title>"));

        var ctx = ReportContext.create().set("title", "Sales Report");
        Workbook result = new ReportEngine().render(wb, ctx);

        assertThat(result.sheet("Sheet1").cell("A1").getValue())
            .isEqualTo(new TextValue("Sales Report"));
    }

    @Test
    void substituteNumberValue() {
        Workbook wb = Workbook.empty();
        wb.addSheet("Sheet1").cell("A1").setValue(new TextValue("<#value count>"));

        var ctx = ReportContext.create().set("count", 42);
        Workbook result = new ReportEngine().render(wb, ctx);

        assertThat(result.sheet("Sheet1").cell("A1").getValue())
            .isEqualTo(new NumberValue(42.0));
    }

    @Test
    void mixedTextSubstitution() {
        Workbook wb = Workbook.empty();
        wb.addSheet("Sheet1").cell("A1").setValue(new TextValue("Total: <#value total>"));

        var ctx = ReportContext.create().set("total", 99.5);
        Workbook result = new ReportEngine().render(wb, ctx);

        assertThat(result.sheet("Sheet1").cell("A1").getValue())
            .isEqualTo(new TextValue("Total: 99.5"));
    }

    @Test
    void expandBand() {
        Workbook wb = Workbook.empty();
        var sheet = wb.addSheet("Sheet1");
        sheet.cell("A1").setValue(new TextValue("Report"));
        sheet.cell("A2").setValue(new TextValue("<#band items>"));
        sheet.cell("A3").setValue(new TextValue("<#value product>"));
        sheet.cell("A4").setValue(new TextValue("</band>"));

        var ctx = ReportContext.create()
            .set("items", List.of(
                Map.of("product", "Widget"),
                Map.of("product", "Gadget")
            ));
        Workbook result = new ReportEngine().render(wb, ctx);

        var out = result.sheet("Sheet1");
        assertThat(out.cell("A1").getValue()).isEqualTo(new TextValue("Report"));
        assertThat(out.cell("A2").getValue()).isEqualTo(new TextValue("Widget"));
        assertThat(out.cell("A3").getValue()).isEqualTo(new TextValue("Gadget"));
    }

    @Test
    void expandBandMultipleColumns() {
        Workbook wb = Workbook.empty();
        var sheet = wb.addSheet("Sheet1");
        sheet.cell("A1").setValue(new TextValue("<#band items>"));
        sheet.cell("A2").setValue(new TextValue("<#value product>"));
        sheet.cell("B2").setValue(new TextValue("<#value qty>"));
        sheet.cell("A3").setValue(new TextValue("</band>"));

        var ctx = ReportContext.create()
            .set("items", List.of(
                Map.of("product", "Widget", "qty", 10),
                Map.of("product", "Gadget", "qty", 5)
            ));
        Workbook result = new ReportEngine().render(wb, ctx);

        var out = result.sheet("Sheet1");
        assertThat(out.cell("A1").getValue()).isEqualTo(new TextValue("Widget"));
        assertThat(out.cell("B1").getValue()).isEqualTo(new NumberValue(10.0));
        assertThat(out.cell("A2").getValue()).isEqualTo(new TextValue("Gadget"));
        assertThat(out.cell("B2").getValue()).isEqualTo(new NumberValue(5.0));
    }

    @Test
    void emptyBandProducesNoRows() {
        Workbook wb = Workbook.empty();
        var sheet = wb.addSheet("Sheet1");
        sheet.cell("A1").setValue(new TextValue("<#band items>"));
        sheet.cell("A2").setValue(new TextValue("<#value product>"));
        sheet.cell("A3").setValue(new TextValue("</band>"));

        var ctx = ReportContext.create().set("items", List.of());
        Workbook result = new ReportEngine().render(wb, ctx);

        var out = result.sheet("Sheet1");
        assertThat(out.cells()).isEmpty();
    }
}
