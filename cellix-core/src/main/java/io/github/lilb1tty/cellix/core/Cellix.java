package io.github.lilb1tty.cellix.core;

import io.github.lilb1tty.cellix.core.exception.CellixReadException;
import io.github.lilb1tty.cellix.core.io.OoxmlReader;
import io.github.lilb1tty.cellix.core.io.OoxmlWriter;
import io.github.lilb1tty.cellix.core.io.SheetStreamReader;
import io.github.lilb1tty.cellix.core.io.internal.SharedStringsTable;
import io.github.lilb1tty.cellix.core.model.Workbook;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipFile;

public final class Cellix {

    private Cellix() {}

    public static Workbook create() {
        return new Workbook();
    }

    public static Workbook create(String firstSheetName) {
        Workbook wb = Workbook.empty();
        wb.addSheet(firstSheetName);
        return wb;
    }

    public static Workbook read(Path path) {
        return new OoxmlReader().read(path);
    }

    public static Workbook read(String path) {
        return read(Path.of(path));
    }

    public static void write(Workbook workbook, Path path) {
        new OoxmlWriter().write(workbook, path);
    }

    public static void write(Workbook workbook, String path) {
        write(workbook, Path.of(path));
    }

    public static SheetStreamReader streamReader(Path path, int sheetIndex) {
        if (sheetIndex < 1) throw new IllegalArgumentException("sheetIndex must be >= 1, got: " + sheetIndex);
        ZipFile zip = null;
        try {
            zip = new ZipFile(path.toFile());
            List<String> sharedStrings = SharedStringsTable.read(zip);
            var entry = zip.getEntry("xl/worksheets/sheet" + sheetIndex + ".xml");
            if (entry == null) {
                throw new CellixReadException("Sheet not found at index " + sheetIndex);
            }
            ZipFile owned = zip;
            zip = null; // transfer ownership to SheetStreamReader
            return new SheetStreamReader(owned, sharedStrings, entry);
        } catch (CellixReadException re) {
            if (zip != null) try { zip.close(); } catch (IOException ignored) {}
            throw re;
        } catch (IOException | javax.xml.stream.XMLStreamException e) {
            if (zip != null) try { zip.close(); } catch (IOException ignored) {}
            throw new CellixReadException("Failed to open XLSX for streaming: " + path, e);
        }
    }

    public static SheetStreamReader streamReader(String path, int sheetIndex) {
        return streamReader(Path.of(path), sheetIndex);
    }
}
