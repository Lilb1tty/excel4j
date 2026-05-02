package io.excel4j.core.model;

import java.util.ArrayList;
import java.util.List;

public final class PivotTable {

    private final String name;
    private final CellRange sourceRange;
    private final List<String> rowFields;
    private final List<DataField> dataFields;
    private final CellRef location;

    private PivotTable(Builder builder) {
        this.name = builder.name;
        this.sourceRange = builder.sourceRange;
        this.rowFields = List.copyOf(builder.rowFields);
        this.dataFields = List.copyOf(builder.dataFields);
        this.location = builder.location;
    }

    public String name()           { return name; }
    public CellRange sourceRange() { return sourceRange; }
    public List<String> rowFields() { return rowFields; }
    public List<DataField> dataFields() { return dataFields; }
    public CellRef location()      { return location; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name = "PivotTable";
        private CellRange sourceRange;
        private final List<String> rowFields = new ArrayList<>();
        private final List<DataField> dataFields = new ArrayList<>();
        private CellRef location;

        public Builder name(String name) { this.name = name; return this; }
        public Builder sourceRange(CellRange range) { this.sourceRange = range; return this; }
        public Builder rowField(String name) { this.rowFields.add(name); return this; }
        public Builder dataField(String name, String aggregation) {
            this.dataFields.add(new DataField(name, aggregation));
            return this;
        }
        public Builder location(CellRef location) { this.location = location; return this; }
        public Builder location(String a1) { this.location = CellRef.of(a1); return this; }
        public PivotTable build() { return new PivotTable(this); }
    }
}
