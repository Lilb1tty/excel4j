package io.excel4j.core.io;

import io.excel4j.core.exception.ExcelWriteException;
import io.excel4j.core.io.internal.DateConverter;
import io.excel4j.core.io.internal.SharedStringsTable;
import io.excel4j.core.io.internal.StyleTable;
import io.excel4j.core.model.*;
import io.excel4j.core.model.style.CellStyle;

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
            writeContentTypes(zip, workbook.sheets().size());
            writeRels(zip);
            writeWorkbook(zip, workbook);
            writeWorkbookRels(zip, workbook.sheets().size());
            writeSharedStrings(zip, sst);
            writeStyles(zip, styleTable);
            for (int i = 0; i < workbook.sheets().size(); i++) {
                writeSheet(zip, workbook.sheets().get(i), i + 1, sst, styleTable);
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

    private void writeContentTypes(ZipOutputStream zip, int sheetCount)
            throws IOException, XMLStreamException {
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

    private void writeWorkbookRels(ZipOutputStream zip, int sheetCount)
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
        }

        if (cell.getFormula() != null) {
            w.writeStartElement("f");
            w.writeCharacters(cell.getFormula());
            w.writeEndElement();
        }

        w.writeEndElement(); // c
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
