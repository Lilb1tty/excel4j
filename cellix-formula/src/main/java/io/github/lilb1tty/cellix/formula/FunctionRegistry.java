package io.github.lilb1tty.cellix.formula;

import java.util.HashMap;
import java.util.Map;

public class FunctionRegistry {

  private final Map<String, CellixFunction> functions = new HashMap<>();

  public FunctionRegistry() {
    registerDefaults();
  }

  public void register(String name, CellixFunction fn) {
    functions.put(name.toUpperCase(), fn);
  }

  public CellixFunction find(String name) {
    return functions.get(name.toUpperCase());
  }

  private void registerDefaults() {
    io.github.lilb1tty.cellix.formula.functions.MathFunctions.registerAll(this);
    io.github.lilb1tty.cellix.formula.functions.LogicFunctions.registerAll(this);
    io.github.lilb1tty.cellix.formula.functions.TextFunctions.registerAll(this);
    io.github.lilb1tty.cellix.formula.functions.DateFunctions.registerAll(this);
    io.github.lilb1tty.cellix.formula.functions.LookupStatFunctions.registerAll(this);
    io.github.lilb1tty.cellix.formula.functions.ArrayFunctions.registerAll(this);
  }
}
