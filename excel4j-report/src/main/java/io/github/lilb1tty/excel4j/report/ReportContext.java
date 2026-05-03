package io.github.lilb1tty.excel4j.report;

import java.util.HashMap;
import java.util.Map;

public final class ReportContext {

    private final Map<String, Object> values;
    private final Object item;

    private ReportContext(Map<String, Object> values, Object item) {
        this.values = new HashMap<>(values);
        this.item = item;
    }

    public static ReportContext create() {
        return new ReportContext(Map.of(), null);
    }

    public ReportContext set(String key, Object value) {
        var copy = new HashMap<>(values);
        copy.put(key, value);
        return new ReportContext(copy, item);
    }

    public Object get(String key) {
        return values.get(key);
    }

    public ReportContext withItem(Object item) {
        return new ReportContext(values, item);
    }

    public Object getItem() {
        return item;
    }

    Map<String, Object> values() {
        return values;
    }
}
