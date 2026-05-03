package io.github.lilb1tty.cellix.report;

import io.github.lilb1tty.cellix.core.model.*;

import java.util.*;

public class ReportEngine {

    public Workbook render(Workbook template, ReportContext ctx) {
        Workbook result = Workbook.empty();

        for (Worksheet sheet : template.sheets()) {
            Worksheet outSheet = result.addSheet(sheet.name().value());
            List<BandExpander.Band> bands = BandExpander.findBands(sheet);

            int maxRow = sheet.cells().keySet().stream()
                .mapToInt(CellRef::row)
                .max().orElse(0);

            int row = 1;
            int outRow = 1;

            while (row <= maxRow) {
                BandExpander.Band band = findBandAtRow(bands, row);

                if (band != null) {
                    Collection<?> items = resolveCollection(ctx, band.name());
                    if (items == null) items = List.of();

                    List<Integer> templateRows = new ArrayList<>();
                    for (int r = band.startRow() + 1; r < band.endRow(); r++) {
                        templateRows.add(r);
                    }

                    for (Object item : items) {
                        ReportContext itemCtx = ctx.withItem(item);
                        for (int templateRow : templateRows) {
                            copyRow(sheet, templateRow, outSheet, outRow, itemCtx);
                            outRow++;
                        }
                    }

                    row = band.endRow() + 1;
                } else {
                    copyRow(sheet, row, outSheet, outRow, ctx);
                    outRow++;
                    row++;
                }
            }
        }

        return result;
    }

    private BandExpander.Band findBandAtRow(List<BandExpander.Band> bands, int row) {
        for (BandExpander.Band band : bands) {
            if (band.startRow() == row) {
                return band;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Collection<?> resolveCollection(ReportContext ctx, String name) {
        Object value = ctx.get(name);
        if (value instanceof Collection<?> c) {
            return c;
        }
        return null;
    }

    private void copyRow(Worksheet src, int srcRow, Worksheet dst, int dstRow, ReportContext ctx) {
        for (var entry : src.cells().entrySet()) {
            if (entry.getKey().row() != srcRow) continue;
            Cell srcCell = entry.getValue();
            int col = entry.getKey().col();
            Cell dstCell = dst.cell(dstRow, col);
            copyAndSubstitute(srcCell, dstCell, ctx);
        }
    }

    private void copyAndSubstitute(Cell src, Cell dst, ReportContext ctx) {
        dst.setStyle(src.getStyle());
        dst.setFormula(src.getFormula());

        CellValue value = src.getValue();
        if (value instanceof TextValue tv) {
            String text = tv.value();
            List<Tag> tags = TagParser.parse(text);

            if (tags.isEmpty()) {
                dst.setValue(value);
                return;
            }

            if (tags.size() == 1 && tags.get(0) instanceof Tag.ValueTag vt) {
                String tagText = "<#value " + vt.name() + ">";
                if (text.trim().equals(tagText)) {
                    dst.setValue(ValueResolver.resolve(ctx, vt.name()));
                    return;
                }
            }

            String result = text;
            for (Tag tag : tags) {
                if (tag instanceof Tag.ValueTag vt) {
                    String tagText = "<#value " + vt.name() + ">";
                    CellValue resolved = ValueResolver.resolve(ctx, vt.name());
                    String replacement = cellValueToString(resolved);
                    result = result.replace(tagText, replacement);
                }
            }
            dst.setValue(new TextValue(result));
        } else {
            dst.setValue(value);
        }
    }

    private String cellValueToString(CellValue value) {
        return switch (value) {
            case TextValue tv -> tv.value();
            case NumberValue nv -> String.valueOf(nv.value());
            case BooleanValue bv -> String.valueOf(bv.value());
            case DateValue dv -> dv.value().toString();
            case DateTimeValue dvt -> dvt.value().toString();
            case ErrorValue ev -> "#ERROR";
            case BlankValue bv -> "";
            case RangeValue rv -> "";
        };
    }
}
