package io.excel4j.core.io;

import io.excel4j.core.exception.ExcelReadException;
import io.excel4j.core.model.*;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class SheetStreamReader implements AutoCloseable {

    private final ZipFile zip;
    private final List<String> sharedStrings;
    private final ZipEntry sheetEntry;
    private static final XMLInputFactory XML_FACTORY = XMLInputFactory.newInstance();

    public SheetStreamReader(ZipFile zip, List<String> sharedStrings, ZipEntry sheetEntry) {
        this.zip = zip;
        this.sharedStrings = List.copyOf(sharedStrings);
        this.sheetEntry = sheetEntry;
    }

    public Stream<Row> stream() {
        try {
            InputStream in = zip.getInputStream(sheetEntry);
            XMLStreamReader r = XML_FACTORY.createXMLStreamReader(in);
            RowIterator it = new RowIterator(r, sharedStrings);
            Spliterator<Row> split = Spliterators.spliteratorUnknownSize(it,
                Spliterator.ORDERED | Spliterator.NONNULL);
            return StreamSupport.stream(split, false)
                .onClose(() -> {
                    try { r.close(); } catch (XMLStreamException ignored) {}
                    try { in.close(); } catch (IOException ignored) {}
                });
        } catch (IOException | XMLStreamException e) {
            throw new ExcelReadException("Failed to stream sheet", e);
        }
    }

    public void forEach(Consumer<Row> consumer) {
        try (Stream<Row> s = stream()) {
            s.forEach(consumer);
        }
    }

    @Override
    public void close() {
        try { zip.close(); } catch (IOException ignored) {}
    }

    // -------------------------------------------------------------------------

    private static final class RowIterator implements Iterator<Row> {

        private final XMLStreamReader r;
        private final List<String> sharedStrings;

        /** Rows buffered by reading ahead from the XML. */
        private final Queue<Row> buffer = new ArrayDeque<>();

        /**
         * Cells accumulated for the row currently being built.
         * Key = column number (1-based).
         */
        private final Map<Integer, CellValue> pendingCells = new LinkedHashMap<>();
        private int pendingRowNum = -1;

        private boolean done;

        // per-cell state
        private int currentCol;
        private String currentType;
        private final StringBuilder currentValue = new StringBuilder();
        private boolean inValue;

        RowIterator(XMLStreamReader r, List<String> sharedStrings) {
            this.r = r;
            this.sharedStrings = sharedStrings;
            fillBuffer();
        }

        /**
         * Read events until we can yield at least one more Row, or until EOF.
         * Handles both:
         *   - sheets WITH &lt;row&gt; elements (standard OOXML)
         *   - sheets WITHOUT &lt;row&gt; elements (excel4j writer omits them)
         */
        private void fillBuffer() {
            if (done) return;
            try {
                while (r.hasNext()) {
                    int event = r.next();
                    if (event == XMLStreamConstants.START_ELEMENT) {
                        switch (r.getLocalName()) {
                            case "row" -> {
                                // Standard OOXML: explicit row element — flush any pending
                                // (shouldn't be any, but be safe) then capture row num
                                String numAttr = r.getAttributeValue(null, "r");
                                int rowNum = numAttr != null
                                    ? Integer.parseInt(numAttr)
                                    : (pendingRowNum < 1 ? 1 : pendingRowNum + 1);
                                // If we already have accumulated cells for a different row,
                                // emit them first (shouldn't happen normally).
                                if (pendingRowNum > 0 && pendingRowNum != rowNum
                                        && !pendingCells.isEmpty()) {
                                    buffer.add(new Row(pendingRowNum, new HashMap<>(pendingCells)));
                                    pendingCells.clear();
                                }
                                pendingRowNum = rowNum;
                            }
                            case "c" -> {
                                String ref = r.getAttributeValue(null, "r");
                                if (ref != null) {
                                    CellRef cellRef = CellRef.of(ref);
                                    int rowNum = cellRef.row();
                                    // If the row changed (no explicit <row> element), emit the old row
                                    if (pendingRowNum > 0 && rowNum != pendingRowNum
                                            && !pendingCells.isEmpty()) {
                                        buffer.add(new Row(pendingRowNum, new HashMap<>(pendingCells)));
                                        pendingCells.clear();
                                        pendingRowNum = rowNum;
                                        currentCol = cellRef.col();
                                        currentType = r.getAttributeValue(null, "t");
                                        currentValue.setLength(0);
                                        inValue = false;
                                        return; // yield row, resume on next hasNext()/next() call
                                    }
                                    pendingRowNum = rowNum;
                                    currentCol = cellRef.col();
                                } else {
                                    currentCol = currentCol + 1;
                                }
                                currentType = r.getAttributeValue(null, "t");
                                currentValue.setLength(0);
                                inValue = false;
                            }
                            case "v" -> inValue = true;
                        }
                    } else if (event == XMLStreamConstants.CHARACTERS && inValue) {
                        currentValue.append(r.getText());
                    } else if (event == XMLStreamConstants.END_ELEMENT) {
                        switch (r.getLocalName()) {
                            case "v" -> inValue = false;
                            case "c" -> {
                                String raw = currentValue.toString();
                                if (!raw.isEmpty() && pendingRowNum > 0) {
                                    pendingCells.put(currentCol, parseCellValue(currentType, raw));
                                }
                            }
                            case "row" -> {
                                // End of an explicit <row> element — emit the row now
                                if (pendingRowNum > 0) {
                                    buffer.add(new Row(pendingRowNum, new HashMap<>(pendingCells)));
                                    pendingCells.clear();
                                    return; // yield the row
                                }
                            }
                            case "sheetData" -> {
                                // End of sheet data — emit any trailing row
                                if (pendingRowNum > 0 && !pendingCells.isEmpty()) {
                                    buffer.add(new Row(pendingRowNum, new HashMap<>(pendingCells)));
                                    pendingCells.clear();
                                }
                                done = true;
                                return;
                            }
                        }
                    }
                }
                // XML stream exhausted — emit any remaining pending row
                if (pendingRowNum > 0 && !pendingCells.isEmpty()) {
                    buffer.add(new Row(pendingRowNum, new HashMap<>(pendingCells)));
                    pendingCells.clear();
                }
                done = true;
            } catch (XMLStreamException e) {
                done = true;
                throw new ExcelReadException("Error reading sheet XML", e);
            }
        }

        private CellValue parseCellValue(String type, String raw) {
            return switch (type != null ? type : "") {
                case "s" -> new TextValue(sharedStrings.get(Integer.parseInt(raw)));
                case "b" -> new BooleanValue("1".equals(raw));
                case "e" -> new ErrorValue(ErrorType.fromExcelString(raw));
                default  -> {
                    try { yield new NumberValue(Double.parseDouble(raw)); }
                    catch (NumberFormatException ex) { yield new TextValue(raw); }
                }
            };
        }

        @Override
        public boolean hasNext() {
            if (!buffer.isEmpty()) return true;
            if (done) return false;
            fillBuffer();
            return !buffer.isEmpty();
        }

        @Override
        public Row next() {
            if (!hasNext()) throw new NoSuchElementException();
            return buffer.poll();
        }
    }
}
