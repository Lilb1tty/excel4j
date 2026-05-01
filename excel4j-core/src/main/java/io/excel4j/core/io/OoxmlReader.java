package io.excel4j.core.io;

import io.excel4j.core.exception.ExcelReadException;
import io.excel4j.core.model.*;
import io.excel4j.core.model.style.CellStyle;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class OoxmlReader {

    private final XMLInputFactory xmlFactory = XMLInputFactory.newInstance();

    public Workbook read(Path path) {
        try (ZipFile zip = new ZipFile(path.toFile())) {
            List<String> sharedStrings = readSharedStrings(zip);
            List<CellStyle> styles = readStyles(zip);
            List<String[]> sheetMeta = readWorkbook(zip); // [name, rId]
            Workbook wb = Workbook.empty();
            for (String[] meta : sheetMeta) {
                wb.addSheet(meta[0]);
            }
            for (int i = 0; i < sheetMeta.size(); i++) {
                readSheet(zip, "xl/worksheets/sheet" + (i + 1) + ".xml",
                    wb.sheet(i + 1), sharedStrings, styles);
            }
            return wb;
        } catch (IOException | XMLStreamException e) {
            throw new ExcelReadException("Failed to read XLSX: " + path, e);
        }
    }

    private List<String> readSharedStrings(ZipFile zip)
            throws IOException, XMLStreamException {
        List<String> result = new ArrayList<>();
        ZipEntry entry = zip.getEntry("xl/sharedStrings.xml");
        if (entry == null) return result;
        try (InputStream in = zip.getInputStream(entry)) {
            XMLStreamReader r = xmlFactory.createXMLStreamReader(in);
            StringBuilder current = null;
            while (r.hasNext()) {
                int event = r.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    if ("si".equals(r.getLocalName())) current = new StringBuilder();
                } else if (event == XMLStreamConstants.CHARACTERS && current != null) {
                    current.append(r.getText());
                } else if (event == XMLStreamConstants.END_ELEMENT) {
                    if ("si".equals(r.getLocalName()) && current != null) {
                        result.add(current.toString());
                        current = null;
                    }
                }
            }
        }
        return result;
    }

    private List<CellStyle> readStyles(ZipFile zip)
            throws IOException, XMLStreamException {
        List<CellStyle> result = new ArrayList<>();
        ZipEntry entry = zip.getEntry("xl/styles.xml");
        if (entry == null) {
            result.add(CellStyle.DEFAULT);
            return result;
        }
        // Minimal style reading: just return DEFAULT for now
        // Full style parsing (fonts, fills, borders, numFmts) is covered in a follow-up
        result.add(CellStyle.DEFAULT);
        return result;
    }

    private List<String[]> readWorkbook(ZipFile zip)
            throws IOException, XMLStreamException {
        List<String[]> sheets = new ArrayList<>();
        ZipEntry entry = zip.getEntry("xl/workbook.xml");
        if (entry == null) throw new ExcelReadException("Missing xl/workbook.xml");
        try (InputStream in = zip.getInputStream(entry)) {
            XMLStreamReader r = xmlFactory.createXMLStreamReader(in);
            while (r.hasNext()) {
                int event = r.next();
                if (event == XMLStreamConstants.START_ELEMENT
                        && "sheet".equals(r.getLocalName())) {
                    String name = r.getAttributeValue(null, "name");
                    String rId = r.getAttributeValue(
                        "http://schemas.openxmlformats.org/officeDocument/2006/relationships", "id");
                    if (rId == null) rId = r.getAttributeValue(null, "r:id");
                    sheets.add(new String[]{name, rId});
                }
            }
        }
        return sheets;
    }

    private void readSheet(ZipFile zip, String entryPath, Worksheet sheet,
            List<String> sharedStrings, List<CellStyle> styles)
            throws IOException, XMLStreamException {
        ZipEntry entry = zip.getEntry(entryPath);
        if (entry == null) return;
        try (InputStream in = zip.getInputStream(entry)) {
            XMLStreamReader r = xmlFactory.createXMLStreamReader(in);
            String currentRef = null;
            String currentType = null;
            String currentStyleIdx = null;
            StringBuilder currentValue = new StringBuilder();
            StringBuilder currentFormula = new StringBuilder();
            boolean inValue = false;
            boolean inFormula = false;

            while (r.hasNext()) {
                int event = r.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    switch (r.getLocalName()) {
                        case "c" -> {
                            currentRef = r.getAttributeValue(null, "r");
                            currentType = r.getAttributeValue(null, "t");
                            currentStyleIdx = r.getAttributeValue(null, "s");
                            currentValue.setLength(0);
                            currentFormula.setLength(0);
                        }
                        case "v" -> inValue = true;
                        case "f" -> inFormula = true;
                    }
                } else if (event == XMLStreamConstants.CHARACTERS) {
                    if (inValue) currentValue.append(r.getText());
                    if (inFormula) currentFormula.append(r.getText());
                } else if (event == XMLStreamConstants.END_ELEMENT) {
                    switch (r.getLocalName()) {
                        case "v" -> inValue = false;
                        case "f" -> inFormula = false;
                        case "c" -> {
                            if (currentRef != null) {
                                Cell cell = sheet.cell(CellRef.of(currentRef));
                                setCellValue(cell, currentType,
                                    currentValue.toString(), sharedStrings);
                                if (!currentFormula.isEmpty()) {
                                    cell.setFormula(currentFormula.toString());
                                }
                            }
                            currentRef = null;
                            currentType = null;
                        }
                    }
                }
            }
        }
    }

    private void setCellValue(Cell cell, String type, String raw,
            List<String> sharedStrings) {
        if (raw.isEmpty()) return;
        switch (type != null ? type : "") {
            case "s" -> cell.setValue(
                new TextValue(sharedStrings.get(Integer.parseInt(raw))));
            case "b" -> cell.setValue(new BooleanValue("1".equals(raw)));
            case "e" -> cell.setValue(new ErrorValue(ErrorType.fromExcelString(raw)));
            default -> {
                try {
                    cell.setValue(new NumberValue(Double.parseDouble(raw)));
                } catch (NumberFormatException e) {
                    cell.setValue(new TextValue(raw));
                }
            }
        }
    }
}
