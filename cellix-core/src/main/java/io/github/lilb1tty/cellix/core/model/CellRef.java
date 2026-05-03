package io.github.lilb1tty.cellix.core.model;

public record CellRef(int row, int col) {

    public CellRef {
        if (row < 1) throw new IllegalArgumentException("row must be >= 1, got: " + row);
        if (col < 1) throw new IllegalArgumentException("col must be >= 1, got: " + col);
    }

    public static CellRef of(String a1) {
        int i = 0;
        while (i < a1.length() && Character.isLetter(a1.charAt(i))) i++;
        String colPart = a1.substring(0, i).toUpperCase();
        int rowNum = Integer.parseInt(a1.substring(i));
        int colNum = 0;
        for (char c : colPart.toCharArray()) {
            colNum = colNum * 26 + (c - 'A' + 1);
        }
        return new CellRef(rowNum, colNum);
    }

    public String toA1() {
        return colToLetters(col) + row;
    }

    public String toA1Absolute() {
        return "$" + colToLetters(col) + "$" + row;
    }

    private static String colToLetters(int col) {
        StringBuilder sb = new StringBuilder();
        int c = col;
        while (c > 0) {
            int rem = (c - 1) % 26;
            sb.insert(0, (char) ('A' + rem));
            c = (c - 1) / 26;
        }
        return sb.toString();
    }
}
