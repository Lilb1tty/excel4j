package io.github.lilb1tty.excel4j.core.io;

import io.github.lilb1tty.excel4j.core.exception.ExcelWriteException;
import io.github.lilb1tty.excel4j.core.io.internal.DateConverter;
import io.github.lilb1tty.excel4j.core.io.internal.SharedStringsTable;
import io.github.lilb1tty.excel4j.core.io.internal.StyleTable;
import io.github.lilb1tty.excel4j.core.model.*;
import io.github.lilb1tty.excel4j.core.model.style.CellStyle;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class OoxmlWriter {

    public void write(Workbook workbook, Path path) {
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(path))) {
            SharedStringsTable sst = buildSharedStrings(workbook);
            StyleTable styleTable = buildStyleTable(workbook);
            int totalCharts = 0;
            int totalPivotTables = 0;
            for (Worksheet sheet : workbook.sheets()) {
                totalCharts += sheet.charts().size();
                totalPivotTables += sheet.pivotTables().size();
            }
            writeContentTypes(zip, workbook, totalCharts, totalPivotTables);
            writeRels(zip);
            writeWorkbook(zip, workbook);
            writeWorkbookRels(zip, workbook.sheets().size(), totalPivotTables);
            writeSharedStrings(zip, sst);
            writeStyles(zip, styleTable);
            int chartIndex = 1;
            int pivotTableIndex = 1;
            int pivotCacheIndex = 1;
            for (int i = 0; i < workbook.sheets().size(); i++) {
                Worksheet sheet = workbook.sheets().get(i);
                int sheetNum = i + 1;
                boolean hasCharts = !sheet.charts().isEmpty();
                boolean hasPivotTables = !sheet.pivotTables().isEmpty();
                int sheetPivotStart = pivotTableIndex;
                int sheetCacheStart = pivotCacheIndex;

                if (hasCharts) {
                    int startChartIndex = chartIndex;
                    for (Chart chart : sheet.charts()) {
                        writeChart(zip, sheet, chart, chartIndex++);
                    }
                    writeDrawing(zip, sheet, sheetNum);
                    writeDrawingRels(zip, sheetNum, startChartIndex, sheet.charts().size());
                }

                if (hasPivotTables) {
                    for (PivotTable pt : sheet.pivotTables()) {
                        writePivotCacheDefinition(zip, pt, sheet, pivotCacheIndex++);
                        writePivotTable(zip, pt, sheetNum, pivotTableIndex++, sheetCacheStart++);
                    }
                }

                if (hasCharts || hasPivotTables) {
                    writeSheetRels(zip, sheetNum, hasCharts, sheetPivotStart, sheet.pivotTables().size());
                }

                writeSheet(zip, sheet, sheetNum, sst, styleTable);
            }
        } catch (IOException | XMLStreamException e) {
            throw new ExcelWriteException("Failed to write XLSX: " + path, e);
        }
    }

    private SharedStringsTable buildSharedStrings(Workbook wb) {
        SharedStringsTable sst = new SharedStringsTable();
        for (Worksheet sheet : wb.sheets()) {
            for (Cell cell : sheet.cells().values()) {
                if (cell.getValue() instanceof TextValue(var s)) sst.add(s);
            }
        }
        return sst;
    }

    private StyleTable buildStyleTable(Workbook wb) {
        StyleTable st = new StyleTable();
        st.add(CellStyle.DEFAULT);
        for (Worksheet sheet : wb.sheets()) {
            for (Cell cell : sheet.cells().values()) {
                st.add(cell.getStyle());
            }
        }
        return st;
    }

    private void writeContentTypes(ZipOutputStream zip, Workbook workbook, int totalCharts,
            int totalPivotTables)
            throws IOException, XMLStreamException {
        int sheetCount = workbook.sheets().size();
        zip.putNextEntry(new ZipEntry("[Content_Types].xml"));
        XMLStreamWriter w = startXml(zip);
        w.writeStartDocument("UTF-8", "1.0");
        w.writeStartElement("Types");
        w.writeDefaultNamespace("http://schemas.openxmlformats.org/package/2006/content-types");
        writeOverride(w, "/_rels/.rels",
            "application/vnd.openxmlformats-package.relationships+xml");
        writeOverride(w, "/xl/workbook.xml",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml");
        writeOverride(w, "/xl/sharedStrings.xml",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sharedStrings+xml");
        writeOverride(w, "/xl/styles.xml",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml");
        for (int i = 1; i <= sheetCount; i++) {
            writeOverride(w, "/xl/worksheets/sheet" + i + ".xml",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml");
        }
        for (int i = 1; i <= totalCharts; i++) {
            writeOverride(w, "/xl/charts/chart" + i + ".xml",
                "application/vnd.openxmlformats-officedocument.drawingml.chart+xml");
        }
        for (int i = 1; i <= sheetCount; i++) {
            if (!workbook.sheets().get(i - 1).charts().isEmpty()) {
                writeOverride(w, "/xl/drawings/drawing" + i + ".xml",
                    "application/vnd.openxmlformats-officedocument.drawingml.spreadsheetDrawing+xml");
            }
        }
        for (int i = 1; i <= totalPivotTables; i++) {
            writeOverride(w, "/xl/pivotCache/pivotCacheDefinition" + i + ".xml",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.pivotCacheDefinition+xml");
            writeOverride(w, "/xl/pivotTables/pivotTable" + i + ".xml",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.pivotTable+xml");
        }
        w.writeEndElement();
        w.writeEndDocument();
        w.flush();
        zip.closeEntry();
    }

    private void writeOverride(XMLStreamWriter w, String partName, String contentType)
            throws XMLStreamException {
        w.writeEmptyElement("Override");
        w.writeAttribute("PartName", partName);
        w.writeAttribute("ContentType", contentType);
    }

    private void writeRels(ZipOutputStream zip) throws IOException, XMLStreamException {
        zip.putNextEntry(new ZipEntry("_rels/.rels"));
        XMLStreamWriter w = startXml(zip);
        w.writeStartDocument("UTF-8", "1.0");
        w.writeStartElement("Relationships");
        w.writeDefaultNamespace("http://schemas.openxmlformats.org/package/2006/relationships");
        w.writeEmptyElement("Relationship");
        w.writeAttribute("Id", "rId1");
        w.writeAttribute("Type",
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument");
        w.writeAttribute("Target", "xl/workbook.xml");
        w.writeEndElement();
        w.writeEndDocument();
        w.flush();
        zip.closeEntry();
    }

    private void writeWorkbook(ZipOutputStream zip, Workbook wb)
            throws IOException, XMLStreamException {
        zip.putNextEntry(new ZipEntry("xl/workbook.xml"));
        XMLStreamWriter w = startXml(zip);
        w.writeStartDocument("UTF-8", "1.0");
        w.writeStartElement("workbook");
        w.writeDefaultNamespace(
            "http://schemas.openxmlformats.org/spreadsheetml/2006/main");
        String rNs = "http://schemas.openxmlformats.org/officeDocument/2006/relationships";
        w.writeNamespace("r", rNs);
        w.writeStartElement("sheets");
        List<Worksheet> sheets = wb.sheets();
        for (int i = 0; i < sheets.size(); i++) {
            w.writeEmptyElement("sheet");
            w.writeAttribute("name", sheets.get(i).name().value());
            w.writeAttribute("sheetId", String.valueOf(i + 1));
            w.writeAttribute(rNs, "id", "rId" + (i + 1));
        }
        w.writeEndElement();
        w.writeEndElement();
        w.writeEndDocument();
        w.flush();
        zip.closeEntry();
    }

    private void writeWorkbookRels(ZipOutputStream zip, int sheetCount, int totalPivotTables)
            throws IOException, XMLStreamException {
        zip.putNextEntry(new ZipEntry("xl/_rels/workbook.xml.rels"));
        XMLStreamWriter w = startXml(zip);
        w.writeStartDocument("UTF-8", "1.0");
        w.writeStartElement("Relationships");
        w.writeDefaultNamespace(
            "http://schemas.openxmlformats.org/package/2006/relationships");
        for (int i = 1; i <= sheetCount; i++) {
            w.writeEmptyElement("Relationship");
            w.writeAttribute("Id", "rId" + i);
            w.writeAttribute("Type",
                "http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet");
            w.writeAttribute("Target", "worksheets/sheet" + i + ".xml");
        }
        w.writeEmptyElement("Relationship");
        w.writeAttribute("Id", "rId" + (sheetCount + 1));
        w.writeAttribute("Type",
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/sharedStrings");
        w.writeAttribute("Target", "sharedStrings.xml");
        w.writeEmptyElement("Relationship");
        w.writeAttribute("Id", "rId" + (sheetCount + 2));
        w.writeAttribute("Type",
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles");
        w.writeAttribute("Target", "styles.xml");
        for (int i = 1; i <= totalPivotTables; i++) {
            w.writeEmptyElement("Relationship");
            w.writeAttribute("Id", "rId" + (sheetCount + 2 + i));
            w.writeAttribute("Type",
                "http://schemas.openxmlformats.org/officeDocument/2006/relationships/pivotCacheDefinition");
            w.writeAttribute("Target", "pivotCache/pivotCacheDefinition" + i + ".xml");
        }
        w.writeEndElement();
        w.writeEndDocument();
        w.flush();
        zip.closeEntry();
    }

    private void writeSharedStrings(ZipOutputStream zip, SharedStringsTable sst)
            throws IOException, XMLStreamException {
        zip.putNextEntry(new ZipEntry("xl/sharedStrings.xml"));
        XMLStreamWriter w = startXml(zip);
        w.writeStartDocument("UTF-8", "1.0");
        w.writeStartElement("sst");
        w.writeDefaultNamespace(
            "http://schemas.openxmlformats.org/spreadsheetml/2006/main");
        w.writeAttribute("count", String.valueOf(sst.size()));
        w.writeAttribute("uniqueCount", String.valueOf(sst.size()));
        for (String s : sst.all()) {
            w.writeStartElement("si");
            w.writeStartElement("t");
            w.writeCharacters(s);
            w.writeEndElement();
            w.writeEndElement();
        }
        w.writeEndElement();
        w.writeEndDocument();
        w.flush();
        zip.closeEntry();
    }

    private void writeStyles(ZipOutputStream zip, StyleTable styleTable)
            throws IOException, XMLStreamException {
        zip.putNextEntry(new ZipEntry("xl/styles.xml"));
        XMLStreamWriter w = startXml(zip);
        w.writeStartDocument("UTF-8", "1.0");
        w.writeStartElement("styleSheet");
        w.writeDefaultNamespace(
            "http://schemas.openxmlformats.org/spreadsheetml/2006/main");
        // fonts
        w.writeStartElement("fonts");
        w.writeAttribute("count", String.valueOf(styleTable.size()));
        for (var style : styleTable.all()) {
            w.writeStartElement("font");
            if (style.font().isBold()) {
                w.writeEmptyElement("b");
            }
            if (style.font().isItalic()) {
                w.writeEmptyElement("i");
            }
            w.writeEmptyElement("sz");
            w.writeAttribute("val", String.valueOf(style.font().size()));
            w.writeEmptyElement("name");
            w.writeAttribute("val", style.font().name());
            w.writeEndElement();
        }
        w.writeEndElement();
        // fills (minimal)
        w.writeStartElement("fills");
        w.writeAttribute("count", "2");
        w.writeStartElement("fill");
        w.writeEmptyElement("patternFill");
        w.writeAttribute("patternType", "none");
        w.writeEndElement();
        w.writeStartElement("fill");
        w.writeEmptyElement("patternFill");
        w.writeAttribute("patternType", "gray125");
        w.writeEndElement();
        w.writeEndElement();
        // borders (minimal)
        w.writeStartElement("borders");
        w.writeAttribute("count", "1");
        w.writeStartElement("border");
        w.writeEmptyElement("left");
        w.writeEmptyElement("right");
        w.writeEmptyElement("top");
        w.writeEmptyElement("bottom");
        w.writeEndElement();
        w.writeEndElement();
        // cellStyleXfs
        w.writeStartElement("cellStyleXfs");
        w.writeAttribute("count", "1");
        w.writeEmptyElement("xf");
        w.writeAttribute("numFmtId", "0");
        w.writeAttribute("fontId", "0");
        w.writeAttribute("fillId", "0");
        w.writeAttribute("borderId", "0");
        w.writeEndElement();
        // cellXfs
        w.writeStartElement("cellXfs");
        w.writeAttribute("count", String.valueOf(styleTable.size()));
        for (int i = 0; i < styleTable.size(); i++) {
            w.writeEmptyElement("xf");
            w.writeAttribute("numFmtId", "0");
            w.writeAttribute("fontId", String.valueOf(i));
            w.writeAttribute("fillId", "0");
            w.writeAttribute("borderId", "0");
            w.writeAttribute("xfId", "0");
        }
        w.writeEndElement();
        w.writeEndElement();
        w.writeEndDocument();
        w.flush();
        zip.closeEntry();
    }

    private void writeSheet(ZipOutputStream zip, Worksheet sheet, int sheetIndex,
            SharedStringsTable sst, StyleTable styleTable)
            throws IOException, XMLStreamException {
        zip.putNextEntry(new ZipEntry("xl/worksheets/sheet" + sheetIndex + ".xml"));
        XMLStreamWriter w = startXml(zip);
        w.writeStartDocument("UTF-8", "1.0");
        w.writeStartElement("worksheet");
        w.writeDefaultNamespace(
            "http://schemas.openxmlformats.org/spreadsheetml/2006/main");
        boolean hasCharts = !sheet.charts().isEmpty();
        boolean hasPivotTables = !sheet.pivotTables().isEmpty();
        String rNs = "http://schemas.openxmlformats.org/officeDocument/2006/relationships";
        if (hasCharts || hasPivotTables) {
            w.writeNamespace("r", rNs);
        }
        w.writeStartElement("sheetData");

        Map<CellRef, Cell> cells = sheet.cells();
        cells.entrySet().stream()
            .sorted(Map.Entry.comparingByKey(
                java.util.Comparator.comparingInt(CellRef::row).thenComparingInt(CellRef::col)))
            .forEach(entry -> {
                try {
                    writeCellXml(w, entry.getValue(), sst, styleTable);
                } catch (XMLStreamException e) {
                    throw new ExcelWriteException("Error writing cell", e);
                }
            });

        w.writeEndElement(); // sheetData
        if (hasCharts) {
            w.writeEmptyElement("drawing");
            w.writeAttribute(rNs, "id", "rId1");
        }
        if (hasPivotTables) {
            int startRId = hasCharts ? 2 : 1;
            w.writeStartElement("tableParts");
            w.writeAttribute("count", String.valueOf(sheet.pivotTables().size()));
            for (int i = 0; i < sheet.pivotTables().size(); i++) {
                w.writeEmptyElement("tablePart");
                w.writeAttribute(rNs, "id", "rId" + (startRId + i));
            }
            w.writeEndElement();
        }
        w.writeEndElement(); // worksheet
        w.writeEndDocument();
        w.flush();
        zip.closeEntry();
    }

    private void writeCellXml(XMLStreamWriter w, Cell cell,
            SharedStringsTable sst, StyleTable styleTable) throws XMLStreamException {
        if (cell.getValue() instanceof BlankValue && cell.getFormula() == null) return;

        w.writeStartElement("c");
        w.writeAttribute("r", cell.getRef().toA1());
        int styleIdx = styleTable.add(cell.getStyle());
        if (styleIdx > 0) w.writeAttribute("s", String.valueOf(styleIdx));

        CellValue value = cell.getValue();
        switch (value) {
            case TextValue(var s) -> {
                w.writeAttribute("t", "s");
                w.writeStartElement("v");
                w.writeCharacters(String.valueOf(sst.add(s)));
                w.writeEndElement();
            }
            case NumberValue(var d) -> {
                w.writeStartElement("v");
                w.writeCharacters(formatDouble(d));
                w.writeEndElement();
            }
            case BooleanValue(var b) -> {
                w.writeAttribute("t", "b");
                w.writeStartElement("v");
                w.writeCharacters(b ? "1" : "0");
                w.writeEndElement();
            }
            case ErrorValue(var t) -> {
                w.writeAttribute("t", "e");
                w.writeStartElement("v");
                w.writeCharacters(t.toExcelString());
                w.writeEndElement();
            }
            case DateValue(var d) -> {
                w.writeStartElement("v");
                w.writeCharacters(formatDouble(DateConverter.toSerial(d)));
                w.writeEndElement();
            }
            case DateTimeValue(var dt) -> {
                w.writeStartElement("v");
                w.writeCharacters(formatDouble(DateConverter.toSerial(dt)));
                w.writeEndElement();
            }
            case BlankValue() -> {
            }
            case RangeValue(var range, var values) -> {
                throw new IllegalStateException(
                        "RangeValue cannot be written as a single cell value: " + range);
            }
        }

        if (cell.getFormula() != null) {
            w.writeStartElement("f");
            w.writeCharacters(cell.getFormula());
            w.writeEndElement();
        }

        w.writeEndElement(); // c
    }

    private void writePivotCacheDefinition(ZipOutputStream zip, PivotTable pt,
            Worksheet sheet, int cacheIndex)
            throws IOException, XMLStreamException {
        zip.putNextEntry(new ZipEntry("xl/pivotCache/pivotCacheDefinition" + cacheIndex + ".xml"));
        XMLStreamWriter w = startXml(zip);
        w.writeStartDocument("UTF-8", "1.0");
        w.writeStartElement("pivotCacheDefinition");
        w.writeDefaultNamespace("http://schemas.openxmlformats.org/spreadsheetml/2006/main");
        w.writeAttribute("refreshOnLoad", "1");
        w.writeAttribute("createdVersion", "6");
        w.writeAttribute("minRefreshableVersion", "3");
        w.writeAttribute("recordCount", "0");

        w.writeStartElement("cacheSource");
        w.writeAttribute("type", "worksheet");
        w.writeStartElement("worksheetSource");
        w.writeAttribute("ref", pt.sourceRange().first().toA1() + ":" + pt.sourceRange().last().toA1());
        w.writeAttribute("sheet", sheet.name().value());
        w.writeEndElement();
        w.writeEndElement();

        List<String> allFields = new java.util.ArrayList<>();
        for (String rf : pt.rowFields()) {
            if (!allFields.contains(rf)) allFields.add(rf);
        }
        for (DataField df : pt.dataFields()) {
            if (!allFields.contains(df.name())) allFields.add(df.name());
        }

        w.writeStartElement("cacheFields");
        w.writeAttribute("count", String.valueOf(allFields.size()));
        for (String field : allFields) {
            w.writeStartElement("cacheField");
            w.writeAttribute("name", field);
            w.writeAttribute("numFmtId", "0");
            w.writeStartElement("sharedItems");
            w.writeAttribute("count", "0");
            w.writeEndElement();
            w.writeEndElement();
        }
        w.writeEndElement();

        w.writeEndElement();
        w.writeEndDocument();
        w.flush();
        zip.closeEntry();
    }

    private void writePivotTable(ZipOutputStream zip, PivotTable pt, int sheetNum,
            int pivotTableIndex, int cacheIndex)
            throws IOException, XMLStreamException {
        zip.putNextEntry(new ZipEntry("xl/pivotTables/pivotTable" + pivotTableIndex + ".xml"));
        XMLStreamWriter w = startXml(zip);
        w.writeStartDocument("UTF-8", "1.0");
        w.writeStartElement("pivotTableDefinition");
        w.writeDefaultNamespace("http://schemas.openxmlformats.org/spreadsheetml/2006/main");
        w.writeAttribute("name", pt.name());
        w.writeAttribute("cacheId", String.valueOf(cacheIndex - 1));
        w.writeAttribute("dataOnRows", "0");
        w.writeAttribute("applyNumberFormats", "0");
        w.writeAttribute("applyBorderFormats", "0");
        w.writeAttribute("applyFontFormats", "0");
        w.writeAttribute("applyPatternFormats", "0");
        w.writeAttribute("applyAlignmentFormats", "0");
        w.writeAttribute("applyWidthHeightFormats", "1");
        w.writeAttribute("autoFormatId", "0");
        w.writeAttribute("ref", pt.location().toA1());
        w.writeAttribute("createdVersion", "6");
        w.writeAttribute("minRefreshableVersion", "3");
        w.writeAttribute("updatedVersion", "6");

        w.writeStartElement("location");
        w.writeAttribute("ref", pt.location().toA1());
        w.writeAttribute("firstHeaderRow", "1");
        w.writeAttribute("firstDataRow", "1");
        w.writeAttribute("firstDataCol", "1");
        w.writeAttribute("rowPageCount", "0");
        w.writeAttribute("colPageCount", "0");
        w.writeEndElement();

        List<String> allFields = new java.util.ArrayList<>();
        for (String rf : pt.rowFields()) {
            if (!allFields.contains(rf)) allFields.add(rf);
        }
        for (DataField df : pt.dataFields()) {
            if (!allFields.contains(df.name())) allFields.add(df.name());
        }

        w.writeStartElement("pivotFields");
        w.writeAttribute("count", String.valueOf(allFields.size()));
        for (int i = 0; i < allFields.size(); i++) {
            String field = allFields.get(i);
            w.writeStartElement("pivotField");
            boolean isRow = pt.rowFields().contains(field);
            boolean isData = pt.dataFields().stream().anyMatch(df -> df.name().equals(field));
            if (isRow) {
                w.writeAttribute("axis", "axisRow");
            }
            if (isData) {
                w.writeAttribute("dataField", "1");
            }
            w.writeAttribute("showAll", "0");
            w.writeStartElement("items");
            w.writeAttribute("count", "1");
            w.writeEmptyElement("item");
            w.writeAttribute("t", "default");
            w.writeEndElement();
            w.writeEndElement();
        }
        w.writeEndElement();

        w.writeStartElement("rowFields");
        w.writeAttribute("count", String.valueOf(pt.rowFields().size()));
        for (String rf : pt.rowFields()) {
            int idx = allFields.indexOf(rf);
            w.writeEmptyElement("field");
            w.writeAttribute("x", String.valueOf(idx));
        }
        w.writeEndElement();

        w.writeStartElement("rowItems");
        w.writeAttribute("count", "1");
        w.writeStartElement("i");
        w.writeEmptyElement("x");
        w.writeEndElement();
        w.writeEndElement();

        w.writeStartElement("dataFields");
        w.writeAttribute("count", String.valueOf(pt.dataFields().size()));
        for (DataField df : pt.dataFields()) {
            int idx = allFields.indexOf(df.name());
            w.writeStartElement("dataField");
            w.writeAttribute("name", capitalize(df.aggregation()) + " of " + df.name());
            w.writeAttribute("fld", String.valueOf(idx));
            w.writeAttribute("baseField", "0");
            w.writeAttribute("baseItem", "0");
            w.writeEndElement();
        }
        w.writeEndElement();

        w.writeStartElement("pivotTableStyleInfo");
        w.writeAttribute("name", "PivotStyleLight16");
        w.writeAttribute("showRowHeaders", "1");
        w.writeAttribute("showColHeaders", "1");
        w.writeAttribute("showRowStripes", "0");
        w.writeAttribute("showColStripes", "0");
        w.writeAttribute("showLastColumn", "1");
        w.writeEndElement();

        w.writeEndElement();
        w.writeEndDocument();
        w.flush();
        zip.closeEntry();
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private void writeChart(ZipOutputStream zip, Worksheet sheet, Chart chart, int chartIndex)
            throws IOException, XMLStreamException {
        zip.putNextEntry(new ZipEntry("xl/charts/chart" + chartIndex + ".xml"));
        XMLStreamWriter w = startXml(zip);
        w.writeStartDocument("UTF-8", "1.0");
        String cNs = "http://schemas.openxmlformats.org/drawingml/2006/chart";
        String aNs = "http://schemas.openxmlformats.org/drawingml/2006/main";
        String rNs = "http://schemas.openxmlformats.org/officeDocument/2006/relationships";
        w.writeStartElement("c:chartSpace");
        w.writeDefaultNamespace(cNs);
        w.writeNamespace("a", aNs);
        w.writeNamespace("r", rNs);
        w.writeStartElement("c:chart");
        if (chart.title() != null && !chart.title().isEmpty()) {
            w.writeStartElement("c:title");
            w.writeStartElement("c:tx");
            w.writeStartElement("c:rich");
            w.writeStartElement("a:bodyPr");
            w.writeEndElement();
            w.writeStartElement("a:lstStyle");
            w.writeEndElement();
            w.writeStartElement("a:p");
            w.writeStartElement("a:r");
            w.writeStartElement("a:t");
            w.writeCharacters(chart.title());
            w.writeEndElement();
            w.writeEndElement();
            w.writeEndElement();
            w.writeEndElement();
            w.writeEndElement();
            w.writeEndElement();
        }
        w.writeStartElement("c:plotArea");
        switch (chart.type()) {
            case BAR -> writeBarChart(w, sheet, chart);
            case LINE -> writeLineChart(w, sheet, chart);
            case PIE -> writePieChart(w, sheet, chart);
        }
        if (chart.type() != ChartType.PIE) {
            writeCatAx(w);
            writeValAx(w);
        }
        w.writeEndElement(); // plotArea
        if (chart.type() != ChartType.PIE) {
            w.writeStartElement("c:legend");
            w.writeEmptyElement("c:legendPos");
            w.writeAttribute("val", "b");
            w.writeEmptyElement("c:overlay");
            w.writeAttribute("val", "0");
            w.writeEndElement();
        }
        w.writeEmptyElement("c:plotVisOnly");
        w.writeAttribute("val", "1");
        w.writeEndElement(); // chart
        w.writeEndElement(); // chartSpace
        w.writeEndDocument();
        w.flush();
        zip.closeEntry();
    }

    private void writeBarChart(XMLStreamWriter w, Worksheet sheet, Chart chart) throws XMLStreamException {
        w.writeStartElement("c:barChart");
        w.writeEmptyElement("c:barDir");
        w.writeAttribute("val", "col");
        w.writeEmptyElement("c:grouping");
        w.writeAttribute("val", "clustered");
        writeSeries(w, sheet, chart);
        w.writeEmptyElement("c:axId");
        w.writeAttribute("val", "48650112");
        w.writeEmptyElement("c:axId");
        w.writeAttribute("val", "48672768");
        w.writeEndElement();
    }

    private void writeLineChart(XMLStreamWriter w, Worksheet sheet, Chart chart) throws XMLStreamException {
        w.writeStartElement("c:lineChart");
        w.writeEmptyElement("c:grouping");
        w.writeAttribute("val", "standard");
        writeSeries(w, sheet, chart);
        w.writeEmptyElement("c:axId");
        w.writeAttribute("val", "48650112");
        w.writeEmptyElement("c:axId");
        w.writeAttribute("val", "48672768");
        w.writeEndElement();
    }

    private void writePieChart(XMLStreamWriter w, Worksheet sheet, Chart chart) throws XMLStreamException {
        w.writeStartElement("c:pieChart");
        writeSeries(w, sheet, chart);
        w.writeEmptyElement("c:firstSliceAng");
        w.writeAttribute("val", "0");
        w.writeEndElement();
    }

    private void writeSeries(XMLStreamWriter w, Worksheet sheet, Chart chart) throws XMLStreamException {
        String sheetName = sheet.name().value();
        String catRef = chart.categories() != null
            ? "'" + sheetName + "'!" + chart.categories().first().toA1Absolute() + ":" + chart.categories().last().toA1Absolute()
            : null;
        int idx = 0;
        for (Series s : chart.series()) {
            w.writeStartElement("c:ser");
            w.writeEmptyElement("c:idx");
            w.writeAttribute("val", String.valueOf(idx));
            w.writeEmptyElement("c:order");
            w.writeAttribute("val", String.valueOf(idx));
            w.writeStartElement("c:tx");
            w.writeStartElement("c:strRef");
            w.writeStartElement("c:f");
            w.writeCharacters("'" + sheetName + "'!" + s.values().first().toA1Absolute());
            w.writeEndElement();
            w.writeEndElement();
            w.writeEndElement();
            if (catRef != null) {
                w.writeStartElement("c:cat");
                w.writeStartElement("c:strRef");
                w.writeStartElement("c:f");
                w.writeCharacters(catRef);
                w.writeEndElement();
                w.writeEndElement();
                w.writeEndElement();
            }
            w.writeStartElement("c:val");
            w.writeStartElement("c:numRef");
            w.writeStartElement("c:f");
            w.writeCharacters("'" + sheetName + "'!" + s.values().first().toA1Absolute() + ":" + s.values().last().toA1Absolute());
            w.writeEndElement();
            w.writeEndElement();
            w.writeEndElement();
            w.writeEndElement();
            idx++;
        }
    }

    private void writeCatAx(XMLStreamWriter w) throws XMLStreamException {
        w.writeStartElement("c:catAx");
        w.writeEmptyElement("c:axId");
        w.writeAttribute("val", "48650112");
        w.writeStartElement("c:scaling");
        w.writeEmptyElement("c:orientation");
        w.writeAttribute("val", "minMax");
        w.writeEndElement();
        w.writeEmptyElement("c:delete");
        w.writeAttribute("val", "0");
        w.writeEmptyElement("c:axPos");
        w.writeAttribute("val", "b");
        w.writeEmptyElement("c:tickLblPos");
        w.writeAttribute("val", "nextTo");
        w.writeEmptyElement("c:crossAx");
        w.writeAttribute("val", "48672768");
        w.writeEmptyElement("c:crosses");
        w.writeAttribute("val", "autoZero");
        w.writeEmptyElement("c:auto");
        w.writeAttribute("val", "1");
        w.writeEmptyElement("c:lblAlgn");
        w.writeAttribute("val", "ctr");
        w.writeEmptyElement("c:lblOffset");
        w.writeAttribute("val", "100");
        w.writeEndElement();
    }

    private void writeValAx(XMLStreamWriter w) throws XMLStreamException {
        w.writeStartElement("c:valAx");
        w.writeEmptyElement("c:axId");
        w.writeAttribute("val", "48672768");
        w.writeStartElement("c:scaling");
        w.writeEmptyElement("c:orientation");
        w.writeAttribute("val", "minMax");
        w.writeEndElement();
        w.writeEmptyElement("c:delete");
        w.writeAttribute("val", "0");
        w.writeEmptyElement("c:axPos");
        w.writeAttribute("val", "l");
        w.writeEmptyElement("c:majorGridlines");
        w.writeEmptyElement("c:tickLblPos");
        w.writeAttribute("val", "nextTo");
        w.writeEmptyElement("c:crossAx");
        w.writeAttribute("val", "48650112");
        w.writeEmptyElement("c:crosses");
        w.writeAttribute("val", "autoZero");
        w.writeEmptyElement("c:crossBetween");
        w.writeAttribute("val", "between");
        w.writeEndElement();
    }

    private void writeDrawing(ZipOutputStream zip, Worksheet sheet, int sheetIndex)
            throws IOException, XMLStreamException {
        zip.putNextEntry(new ZipEntry("xl/drawings/drawing" + sheetIndex + ".xml"));
        XMLStreamWriter w = startXml(zip);
        w.writeStartDocument("UTF-8", "1.0");
        String xdrNs = "http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing";
        String aNs = "http://schemas.openxmlformats.org/drawingml/2006/main";
        String rNs = "http://schemas.openxmlformats.org/officeDocument/2006/relationships";
        String cNs = "http://schemas.openxmlformats.org/drawingml/2006/chart";
        w.writeStartElement("xdr:wsDr");
        w.writeDefaultNamespace(xdrNs);
        w.writeNamespace("a", aNs);
        w.writeNamespace("r", rNs);
        w.writeNamespace("c", cNs);
        int chartIdx = 1;
        for (Chart chart : sheet.charts()) {
            w.writeStartElement("xdr:twoCellAnchor");
            w.writeAttribute("editAs", "oneCell");
            w.writeStartElement("xdr:from");
            w.writeStartElement("xdr:col");
            w.writeCharacters(String.valueOf(chart.fromCol() - 1));
            w.writeEndElement();
            w.writeStartElement("xdr:colOff");
            w.writeCharacters("0");
            w.writeEndElement();
            w.writeStartElement("xdr:row");
            w.writeCharacters(String.valueOf(chart.fromRow() - 1));
            w.writeEndElement();
            w.writeStartElement("xdr:rowOff");
            w.writeCharacters("0");
            w.writeEndElement();
            w.writeEndElement();
            w.writeStartElement("xdr:to");
            w.writeStartElement("xdr:col");
            w.writeCharacters(String.valueOf(chart.toCol() - 1));
            w.writeEndElement();
            w.writeStartElement("xdr:colOff");
            w.writeCharacters("0");
            w.writeEndElement();
            w.writeStartElement("xdr:row");
            w.writeCharacters(String.valueOf(chart.toRow() - 1));
            w.writeEndElement();
            w.writeStartElement("xdr:rowOff");
            w.writeCharacters("0");
            w.writeEndElement();
            w.writeEndElement();
            w.writeStartElement("xdr:graphicFrame");
            w.writeAttribute("macro", "");
            w.writeStartElement("xdr:nvGraphicFramePr");
            w.writeStartElement("xdr:cNvPr");
            w.writeAttribute("id", String.valueOf(chartIdx + 1));
            w.writeAttribute("name", "Chart " + chartIdx);
            w.writeEndElement();
            w.writeStartElement("xdr:cNvGraphicFramePr");
            w.writeEndElement();
            w.writeEndElement();
            w.writeStartElement("xdr:xfrm");
            w.writeStartElement("a:off");
            w.writeAttribute("x", "0");
            w.writeAttribute("y", "0");
            w.writeEndElement();
            w.writeStartElement("a:ext");
            w.writeAttribute("cx", "0");
            w.writeAttribute("cy", "0");
            w.writeEndElement();
            w.writeEndElement();
            w.writeStartElement("a:graphic");
            w.writeStartElement("a:graphicData");
            w.writeAttribute("uri", "http://schemas.openxmlformats.org/drawingml/2006/chart");
            w.writeStartElement("c", "chart", cNs);
            w.writeAttribute(rNs, "id", "rId" + chartIdx);
            w.writeEndElement();
            w.writeEndElement();
            w.writeEndElement();
            w.writeEndElement();
            w.writeStartElement("xdr:clientData");
            w.writeEndElement();
            w.writeEndElement();
            chartIdx++;
        }
        w.writeEndElement();
        w.writeEndDocument();
        w.flush();
        zip.closeEntry();
    }

    private void writeDrawingRels(ZipOutputStream zip, int sheetIndex, int startChartIndex, int chartCount)
            throws IOException, XMLStreamException {
        zip.putNextEntry(new ZipEntry("xl/drawings/_rels/drawing" + sheetIndex + ".xml.rels"));
        XMLStreamWriter w = startXml(zip);
        w.writeStartDocument("UTF-8", "1.0");
        w.writeStartElement("Relationships");
        w.writeDefaultNamespace("http://schemas.openxmlformats.org/package/2006/relationships");
        for (int i = 0; i < chartCount; i++) {
            w.writeEmptyElement("Relationship");
            w.writeAttribute("Id", "rId" + (i + 1));
            w.writeAttribute("Type",
                "http://schemas.openxmlformats.org/officeDocument/2006/relationships/chart");
            w.writeAttribute("Target", "../charts/chart" + (startChartIndex + i) + ".xml");
        }
        w.writeEndElement();
        w.writeEndDocument();
        w.flush();
        zip.closeEntry();
    }

    private void writeSheetRels(ZipOutputStream zip, int sheetIndex, boolean hasCharts,
            int sheetPivotStart, int pivotTableCount)
            throws IOException, XMLStreamException {
        zip.putNextEntry(new ZipEntry("xl/worksheets/_rels/sheet" + sheetIndex + ".xml.rels"));
        XMLStreamWriter w = startXml(zip);
        w.writeStartDocument("UTF-8", "1.0");
        w.writeStartElement("Relationships");
        w.writeDefaultNamespace("http://schemas.openxmlformats.org/package/2006/relationships");
        int rId = 1;
        if (hasCharts) {
            w.writeEmptyElement("Relationship");
            w.writeAttribute("Id", "rId" + rId++);
            w.writeAttribute("Type",
                "http://schemas.openxmlformats.org/officeDocument/2006/relationships/drawing");
            w.writeAttribute("Target", "../drawings/drawing" + sheetIndex + ".xml");
        }
        for (int i = 0; i < pivotTableCount; i++) {
            w.writeEmptyElement("Relationship");
            w.writeAttribute("Id", "rId" + rId++);
            w.writeAttribute("Type",
                "http://schemas.openxmlformats.org/officeDocument/2006/relationships/pivotTable");
            w.writeAttribute("Target", "../pivotTables/pivotTable" + (sheetPivotStart + i) + ".xml");
        }
        w.writeEndElement();
        w.writeEndDocument();
        w.flush();
        zip.closeEntry();
    }

    private String formatDouble(double d) {
        if (d == Math.floor(d) && !Double.isInfinite(d)) {
            return String.valueOf((long) d);
        }
        return String.valueOf(d);
    }

    private XMLStreamWriter startXml(OutputStream out) throws XMLStreamException {
        return XMLOutputFactory.newInstance().createXMLStreamWriter(out, "UTF-8");
    }
}
