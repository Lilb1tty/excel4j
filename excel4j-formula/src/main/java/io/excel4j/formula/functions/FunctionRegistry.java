package io.excel4j.formula.functions;

import java.util.HashMap;
import java.util.Map;

public class FunctionRegistry {

    private final Map<String, ExcelFunction> functions = new HashMap<>();

    public FunctionRegistry() {
    }

    public void register(String name, ExcelFunction function) {
        functions.put(name.toUpperCase(), function);
    }

    public ExcelFunction find(String name) {
        return functions.get(name.toUpperCase());
    }
}
