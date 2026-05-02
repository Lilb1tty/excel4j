package io.excel4j.core.model;

import java.util.ArrayList;
import java.util.List;

public final class Chart {

    private final String title;
    private final ChartType type;
    private final CellRange categories;
    private final List<Series> series;
    private final int fromRow;
    private final int fromCol;
    private final int toRow;
    private final int toCol;

    private Chart(Builder builder) {
        this.title = builder.title;
        this.type = builder.type;
        this.categories = builder.categories;
        this.series = List.copyOf(builder.series);
        this.fromRow = builder.fromRow;
        this.fromCol = builder.fromCol;
        this.toRow = builder.toRow;
        this.toCol = builder.toCol;
    }

    public String title()       { return title; }
    public ChartType type()     { return type; }
    public CellRange categories() { return categories; }
    public List<Series> series()  { return series; }
    public int fromRow()        { return fromRow; }
    public int fromCol()        { return fromCol; }
    public int toRow()          { return toRow; }
    public int toCol()          { return toCol; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String title = "";
        private ChartType type = ChartType.BAR;
        private CellRange categories;
        private final List<Series> series = new ArrayList<>();
        private int fromRow;
        private int fromCol;
        private int toRow;
        private int toCol;

        public Builder title(String title) { this.title = title; return this; }
        public Builder type(ChartType type) { this.type = type; return this; }
        public Builder categories(CellRange categories) { this.categories = categories; return this; }
        public Builder addSeries(Series s) { this.series.add(s); return this; }
        public Builder addSeries(String name, CellRange values) { this.series.add(new Series(name, values)); return this; }
        public Builder position(int fromRow, int fromCol, int toRow, int toCol) {
            this.fromRow = fromRow; this.fromCol = fromCol;
            this.toRow = toRow; this.toCol = toCol;
            return this;
        }
        public Chart build() { return new Chart(this); }
    }
}
