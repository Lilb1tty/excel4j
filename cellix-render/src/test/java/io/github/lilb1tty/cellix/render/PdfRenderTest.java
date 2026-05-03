package io.github.lilb1tty.cellix.render;

import io.github.lilb1tty.cellix.core.Cellix;
import io.github.lilb1tty.cellix.core.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

class PdfRenderTest {

    @TempDir
    Path tempDir;

    private static Workbook sampleWorkbook() {
        Workbook wb = Cellix.create();
        Worksheet sheet = wb.sheet(1);
        sheet.cell("A1").setValue(new TextValue("Name"));
        sheet.cell("B1").setValue(new TextValue("Score"));
        sheet.cell("A2").setValue(new TextValue("Alice"));
        sheet.cell("B2").setValue(new NumberValue(95));
        sheet.cell("A3").setValue(new TextValue("Bob"));
        sheet.cell("B3").setValue(new NumberValue(82));
        return wb;
    }

    @Test
    void toPdf_workbook_writesValidPdfFile() throws IOException {
        Path out = tempDir.resolve("report.pdf");
        PdfRenderer.toPdf(sampleWorkbook(), out);

        assertThat(out).exists();
        byte[] bytes = Files.readAllBytes(out);
        assertThat(bytes.length).isGreaterThan(0);
        assertThat(new String(bytes, 0, 8)).isEqualTo("%PDF-1.4");
    }

    @Test
    void toPdf_workbook_containsPdfStructure() throws IOException {
        Path out = tempDir.resolve("structured.pdf");
        PdfRenderer.toPdf(sampleWorkbook(), out);

        String content = new String(Files.readAllBytes(out), java.nio.charset.StandardCharsets.ISO_8859_1);
        assertThat(content).contains("xref");
        assertThat(content).contains("trailer");
        assertThat(content).contains("startxref");
        assertThat(content).contains("%%EOF");
        assertThat(content).contains("/Type /Catalog");
        assertThat(content).contains("/Type /Pages");
        assertThat(content).contains("/Type /Page");
        assertThat(content).contains("/Subtype /Image");
    }

    @Test
    void toPdf_singleSheet_writesOnePage() throws IOException {
        Path out = tempDir.resolve("single.pdf");
        Workbook wb = Cellix.create();
        Worksheet sheet = wb.sheet(1);
        sheet.cell("A1").setValue(new TextValue("Hello"));
        PdfRenderer.toPdf(sheet, out);

        assertThat(out).exists();
        String content = new String(Files.readAllBytes(out), java.nio.charset.StandardCharsets.ISO_8859_1);
        assertThat(content).contains("%PDF-1.4");
        assertThat(content).contains("/Count 1");
    }

    @Test
    void toPdf_multiSheet_writesMultiplePages() throws IOException {
        Workbook wb = Cellix.create();
        wb.sheet(1).cell("A1").setValue(new TextValue("Page 1"));
        wb.addSheet("Sheet2").cell("A1").setValue(new TextValue("Page 2"));
        wb.addSheet("Sheet3").cell("A1").setValue(new TextValue("Page 3"));

        Path out = tempDir.resolve("multi.pdf");
        PdfRenderer.toPdf(wb, out);

        String content = new String(Files.readAllBytes(out), java.nio.charset.StandardCharsets.ISO_8859_1);
        assertThat(content).contains("/Count 3");
        // Three image objects
        assertThat(content).contains("/Im1");
        assertThat(content).contains("/Im2");
        assertThat(content).contains("/Im3");
    }

    @Test
    void toPdf_customRenderOptions_producesFile() throws IOException {
        Path out = tempDir.resolve("custom.pdf");
        RenderOptions opts = new RenderOptions(64, 18, 9, 2);
        PdfRenderer.toPdf(sampleWorkbook(), out, opts);
        assertThat(out).exists();
        assertThat(out.toFile().length()).isGreaterThan(0);
    }
}
