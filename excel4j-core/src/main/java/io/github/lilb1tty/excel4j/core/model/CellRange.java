package io.github.lilb1tty.excel4j.core.model;

public record CellRange(CellRef first, CellRef last) {

    public boolean contains(CellRef ref) {
        return ref.row() >= first.row() && ref.row() <= last.row()
            && ref.col() >= first.col() && ref.col() <= last.col();
    }

    public static CellRange of(String firstA1, String lastA1) {
        return new CellRange(CellRef.of(firstA1), CellRef.of(lastA1));
    }
}
