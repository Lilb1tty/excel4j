package io.github.lilb1tty.cellix.core.io;

import io.github.lilb1tty.cellix.core.Cellix;
import io.github.lilb1tty.cellix.core.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.assertj.core.api.Assertions.*;

class PivotTableWriteTest {

    @TempDir
    Path tempDir;

    @Test
    void writePivotTable() throws IOException {
        Workbook wb = Cellix.create();
        Worksheet sheet = wb.sheet("Sheet1");

        sheet.cell("A1").setValue(new TextValue("Product"));
        sheet.cell("B1").setValue(new TextValue("Region"));
        sheet.cell("C1").setValue(new TextValue("Sales"));
        sheet.cell("A2").setValue(new TextValue("Widget"));
        sheet.cell("B2").setValue(new TextValue("North"));
        sheet.cell("C2").setValue(new NumberValue(100));

        PivotTable pt = PivotTable.builder()
            .name("SalesPivot")
            .sourceRange(CellRange.of("A1", "C2"))
            .rowField("Product")
            .dataField("Sales", "sum")
            .location("E1")
            .build();
        sheet.addPivotTable(pt);

        Path path = tempDir.resolve("pivot-test.xlsx");
        Cellix.write(wb, path);

        try (ZipFile zip = new ZipFile(path.toFile())) {
            assertThat(zip.getEntry("xl/pivotCache/pivotCacheDefinition1.xml")).isNotNull();
            assertThat(zip.getEntry("xl/pivotTables/pivotTable1.xml")).isNotNull();
            assertThat(zip.getEntry("xl/worksheets/_rels/sheet1.xml.rels")).isNotNull();

            String cacheXml = new String(zip.getInputStream(zip.getEntry("xl/pivotCache/pivotCacheDefinition1.xml")).readAllBytes());
            assertThat(cacheXml).contains("A1:C2");
            assertThat(cacheXml).contains("Product");
            assertThat(cacheXml).contains("Sales");

            String ptXml = new String(zip.getInputStream(zip.getEntry("xl/pivotTables/pivotTable1.xml")).readAllBytes());
            assertThat(ptXml).contains("SalesPivot");
            assertThat(ptXml).contains("cacheId=\"0\"");
            assertThat(ptXml).contains("E1");
            assertThat(ptXml).contains("Sum of Sales");
            assertThat(ptXml).contains("axisRow");

            String sheetRels = new String(zip.getInputStream(zip.getEntry("xl/worksheets/_rels/sheet1.xml.rels")).readAllBytes());
            assertThat(sheetRels).contains("pivotTable");
            assertThat(sheetRels).contains("pivotTable1.xml");

            String sheetXml = new String(zip.getInputStream(zip.getEntry("xl/worksheets/sheet1.xml")).readAllBytes());
            assertThat(sheetXml).contains("tableParts");
        }
    }

    @Test
    void writeWorkbookWithoutPivotTablesHasNoPivotParts() throws IOException {
        Workbook wb = Cellix.create();
        wb.sheet("Sheet1").cell("A1").setValue(new TextValue("Hello"));

        Path path = tempDir.resolve("no-pivot.xlsx");
        Cellix.write(wb, path);

        try (ZipFile zip = new ZipFile(path.toFile())) {
            assertThat(zip.getEntry("xl/pivotCache/pivotCacheDefinition1.xml")).isNull();
            assertThat(zip.getEntry("xl/pivotTables/pivotTable1.xml")).isNull();
            assertThat(zip.getEntry("xl/worksheets/_rels/sheet1.xml.rels")).isNull();
        }
    }

    @Test
    void writeMultiplePivotTables() throws IOException {
        Workbook wb = Cellix.create();
        Worksheet sheet = wb.sheet("Sheet1");

        sheet.cell("A1").setValue(new TextValue("Item"));
        sheet.cell("B1").setValue(new TextValue("Qty"));
        sheet.cell("A2").setValue(new TextValue("X"));
        sheet.cell("B2").setValue(new NumberValue(5));

        sheet.addPivotTable(PivotTable.builder()
            .name("PT1")
            .sourceRange(CellRange.of("A1", "B2"))
            .rowField("Item")
            .dataField("Qty", "sum")
            .location("D1")
            .build());

        sheet.addPivotTable(PivotTable.builder()
            .name("PT2")
            .sourceRange(CellRange.of("A1", "B2"))
            .rowField("Item")
            .dataField("Qty", "count")
            .location("D10")
            .build());

        Path path = tempDir.resolve("multi-pivot.xlsx");
        Cellix.write(wb, path);

        try (ZipFile zip = new ZipFile(path.toFile())) {
            assertThat(zip.getEntry("xl/pivotCache/pivotCacheDefinition1.xml")).isNotNull();
            assertThat(zip.getEntry("xl/pivotCache/pivotCacheDefinition2.xml")).isNotNull();
            assertThat(zip.getEntry("xl/pivotTables/pivotTable1.xml")).isNotNull();
            assertThat(zip.getEntry("xl/pivotTables/pivotTable2.xml")).isNotNull();

            String sheetRels = new String(zip.getInputStream(zip.getEntry("xl/worksheets/_rels/sheet1.xml.rels")).readAllBytes());
            assertThat(sheetRels).contains("pivotTable1.xml");
            assertThat(sheetRels).contains("pivotTable2.xml");
        }
    }
}
