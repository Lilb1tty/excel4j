package io.excel4j.core;

import io.excel4j.core.io.OoxmlReader;
import io.excel4j.core.io.OoxmlWriter;
import io.excel4j.core.model.Workbook;

import java.nio.file.Path;

public final class Excel {

    private Excel() {}

    public static Workbook create() {
        return new Workbook();
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
}
