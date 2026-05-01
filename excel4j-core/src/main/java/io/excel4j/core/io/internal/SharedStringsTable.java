package io.excel4j.core.io.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SharedStringsTable {

    private final List<String> strings = new ArrayList<>();
    private final Map<String, Integer> index = new HashMap<>();

    public int add(String s) {
        return index.computeIfAbsent(s, k -> {
            int idx = strings.size();
            strings.add(k);
            return idx;
        });
    }

    public String get(int idx) {
        return strings.get(idx);
    }

    public int size() {
        return strings.size();
    }

    public List<String> all() {
        return List.copyOf(strings);
    }
}
