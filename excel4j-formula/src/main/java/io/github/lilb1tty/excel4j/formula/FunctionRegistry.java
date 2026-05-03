package io.github.lilb1tty.excel4j.formula;

import java.util.HashMap;
import java.util.Map;

public class FunctionRegistry {

  private final Map<String, ExcelFunction> functions = new HashMap<>();

  public FunctionRegistry() {
    registerDefaults();
  }

  public void register(String name, ExcelFunction fn) {
    functions.put(name.toUpperCase(), fn);
  }

  public ExcelFunction find(String name) {
    return functions.get(name.toUpperCase());
  }

  private void registerDefaults() {
    io.github.lilb1tty.excel4j.formula.functions.MathFunctions.registerAll(this);
    io.github.lilb1tty.excel4j.formula.functions.LogicFunctions.registerAll(this);
    io.github.lilb1tty.excel4j.formula.functions.TextFunctions.registerAll(this);
    io.github.lilb1tty.excel4j.formula.functions.DateFunctions.registerAll(this);
    io.github.lilb1tty.excel4j.formula.functions.LookupStatFunctions.registerAll(this);
    io.github.lilb1tty.excel4j.formula.functions.ArrayFunctions.registerAll(this);
  }
}
