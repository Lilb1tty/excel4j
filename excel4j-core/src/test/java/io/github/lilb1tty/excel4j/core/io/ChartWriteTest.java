package io.github.lilb1tty.excel4j.core.io;

import io.github.lilb1tty.excel4j.core.Excel;
import io.github.lilb1tty.excel4j.core.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.assertj.core.api.Assertions.*;

class ChartWriteTest {

    @TempDir
    Path tempDir;

    @Test
    void writeBarChart() throws IOException {
        Workbook wb = Excel.create();
        Worksheet sheet = wb.sheet("Sheet1");

        sheet.cell("A1").setValue(new TextValue("Q1"));
        sheet.cell("A2").setValue(new TextValue("Q2"));
        sheet.cell("A3").setValue(new TextValue("Q3"));
        sheet.cell("B1").setValue(new NumberValue(100));
        sheet.cell("B2").setValue(new NumberValue(200));
        sheet.cell("B3").setValue(new NumberValue(150));

        Chart chart = Chart.builder()
            .title("Sales")
            .type(ChartType.BAR)
            .categories(CellRange.of("A2", "A3"))
            .addSeries("Revenue", CellRange.of("B2", "B3"))
            .position(5, 1, 20, 10)
            .build();
        sheet.addChart(chart);

        Path path = tempDir.resolve("chart-test.xlsx");
        Excel.write(wb, path);

        try (ZipFile zip = new ZipFile(path.toFile())) {
            assertThat(zip.getEntry("xl/charts/chart1.xml")).isNotNull();
            assertThat(zip.getEntry("xl/drawings/drawing1.xml")).isNotNull();
            assertThat(zip.getEntry("xl/drawings/_rels/drawing1.xml.rels")).isNotNull();
            assertThat(zip.getEntry("xl/worksheets/_rels/sheet1.xml.rels")).isNotNull();

            String chartXml = new String(zip.getInputStream(zip.getEntry("xl/charts/chart1.xml")).readAllBytes());
            assertThat(chartXml).contains("Sales");
            assertThat(chartXml).contains("c:barChart");
            assertThat(chartXml).contains("$A$2:$A$3");
            assertThat(chartXml).contains("$B$2:$B$3");
        }
    }

    @Test
    void writeLineChart() throws IOException {
        Workbook wb = Excel.create();
        Worksheet sheet = wb.sheet("Sheet1");

        sheet.cell("A1").setValue(new TextValue("Jan"));
        sheet.cell("A2").setValue(new TextValue("Feb"));
        sheet.cell("B1").setValue(new NumberValue(10));
        sheet.cell("B2").setValue(new NumberValue(20));

        Chart chart = Chart.builder()
            .title("Trend")
            .type(ChartType.LINE)
            .categories(CellRange.of("A1", "A2"))
            .addSeries("Data", CellRange.of("B1", "B2"))
            .position(1, 3, 15, 10)
            .build();
        sheet.addChart(chart);

        Path path = tempDir.resolve("line-chart.xlsx");
        Excel.write(wb, path);

        try (ZipFile zip = new ZipFile(path.toFile())) {
            String chartXml = new String(zip.getInputStream(zip.getEntry("xl/charts/chart1.xml")).readAllBytes());
            assertThat(chartXml).contains("c:lineChart");
            assertThat(chartXml).contains("Trend");
        }
    }

    @Test
    void writePieChart() throws IOException {
        Workbook wb = Excel.create();
        Worksheet sheet = wb.sheet("Sheet1");

        sheet.cell("A1").setValue(new TextValue("A"));
        sheet.cell("A2").setValue(new TextValue("B"));
        sheet.cell("B1").setValue(new NumberValue(30));
        sheet.cell("B2").setValue(new NumberValue(70));

        Chart chart = Chart.builder()
            .title("Share")
            .type(ChartType.PIE)
            .categories(CellRange.of("A1", "A2"))
            .addSeries("Slice", CellRange.of("B1", "B2"))
            .position(1, 3, 15, 10)
            .build();
        sheet.addChart(chart);

        Path path = tempDir.resolve("pie-chart.xlsx");
        Excel.write(wb, path);

        try (ZipFile zip = new ZipFile(path.toFile())) {
            String chartXml = new String(zip.getInputStream(zip.getEntry("xl/charts/chart1.xml")).readAllBytes());
            assertThat(chartXml).contains("c:pieChart");
            assertThat(chartXml).contains("Share");
        }
    }

    @Test
    void writeMultipleChartsOnOneSheet() throws IOException {
        Workbook wb = Excel.create();
        Worksheet sheet = wb.sheet("Sheet1");

        sheet.cell("A1").setValue(new NumberValue(1));
        sheet.cell("A2").setValue(new NumberValue(2));

        sheet.addChart(Chart.builder()
            .title("Chart 1").type(ChartType.BAR)
            .addSeries("S1", CellRange.of("A1", "A2"))
            .position(1, 3, 10, 10).build());
        sheet.addChart(Chart.builder()
            .title("Chart 2").type(ChartType.LINE)
            .addSeries("S2", CellRange.of("A1", "A2"))
            .position(11, 3, 20, 10).build());

        Path path = tempDir.resolve("multi-chart.xlsx");
        Excel.write(wb, path);

        try (ZipFile zip = new ZipFile(path.toFile())) {
            assertThat(zip.getEntry("xl/charts/chart1.xml")).isNotNull();
            assertThat(zip.getEntry("xl/charts/chart2.xml")).isNotNull();
            assertThat(zip.getEntry("xl/drawings/drawing1.xml")).isNotNull();
        }
    }

    @Test
    void writeWorkbookWithoutChartsHasNoChartParts() throws IOException {
        Workbook wb = Excel.create();
        wb.sheet("Sheet1").cell("A1").setValue(new TextValue("Hello"));

        Path path = tempDir.resolve("no-chart.xlsx");
        Excel.write(wb, path);

        try (ZipFile zip = new ZipFile(path.toFile())) {
            assertThat(zip.getEntry("xl/charts/chart1.xml")).isNull();
            assertThat(zip.getEntry("xl/drawings/drawing1.xml")).isNull();
            assertThat(zip.getEntry("xl/worksheets/_rels/sheet1.xml.rels")).isNull();
        }
    }
}
