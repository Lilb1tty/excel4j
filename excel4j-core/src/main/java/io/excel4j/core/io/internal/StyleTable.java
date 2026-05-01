package io.excel4j.core.io.internal;

import io.excel4j.core.model.style.CellStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StyleTable {

    private final List<CellStyle> styles = new ArrayList<>();
    private final Map<CellStyle, Integer> index = new HashMap<>();

    public int add(CellStyle style) {
        return index.computeIfAbsent(style, k -> {
            int idx = styles.size();
            styles.add(k);
            return idx;
        });
    }

    public CellStyle get(int idx) {
        return styles.get(idx);
    }

    public int size() {
        return styles.size();
    }

    public List<CellStyle> all() {
        return List.copyOf(styles);
    }
}
