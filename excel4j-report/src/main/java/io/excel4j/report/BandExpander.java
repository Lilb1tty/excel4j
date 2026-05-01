package io.excel4j.report;

import io.excel4j.core.model.CellRef;
import io.excel4j.core.model.TextValue;
import io.excel4j.core.model.Worksheet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class BandExpander {

    public record Band(int startRow, int endRow, String name) {}

    private BandExpander() {}

    public static List<Band> findBands(Worksheet sheet) {
        List<Band> bands = new ArrayList<>();

        for (var entry : sheet.cells().entrySet()) {
            if (!(entry.getValue().getValue() instanceof TextValue tv)) continue;
            var tags = TagParser.parse(tv.value());
            for (Tag tag : tags) {
                if (tag instanceof Tag.BandStartTag bt) {
                    int startRow = entry.getKey().row();
                    int endRow = findBandEndRow(sheet, startRow);
                    bands.add(new Band(startRow, endRow, bt.name()));
                }
            }
        }

        bands.sort(Comparator.comparingInt(Band::startRow).reversed());
        return bands;
    }

    private static int findBandEndRow(Worksheet sheet, int startRow) {
        List<CellRef> endRefs = new ArrayList<>();
        for (var entry : sheet.cells().entrySet()) {
            if (entry.getKey().row() <= startRow) continue;
            if (!(entry.getValue().getValue() instanceof TextValue tv)) continue;
            var tags = TagParser.parse(tv.value());
            for (Tag tag : tags) {
                if (tag instanceof Tag.BandEndTag) {
                    endRefs.add(entry.getKey());
                }
            }
        }
        if (endRefs.isEmpty()) {
            throw new IllegalStateException("Unclosed band starting at row " + startRow);
        }
        return Collections.min(endRefs, Comparator.comparingInt(CellRef::row)).row();
    }
}
