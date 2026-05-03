package io.github.lilb1tty.excel4j.formula.functions;

import io.github.lilb1tty.excel4j.core.model.*;
import io.github.lilb1tty.excel4j.formula.EvalContext;
import io.github.lilb1tty.excel4j.formula.FunctionRegistry;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class TextFunctionsExtTest {

  private static final EvalContext CTX = new EvalContext() {
    @Override public CellValue resolve(CellRef ref) { return new BlankValue(); }
    @Override public CellValue resolve(WorksheetName sheet, CellRef ref) { return new BlankValue(); }
  };

  private CellValue call(String name, CellValue... args) {
    FunctionRegistry reg = new FunctionRegistry();
    TextFunctions.registerAll(reg);
    return reg.find(name).evaluate(List.of(args), CTX);
  }

  @Test
  void exact() {
    assertThat(call("EXACT", new TextValue("abc"), new TextValue("abc")))
        .isEqualTo(new BooleanValue(true));
    assertThat(call("EXACT", new TextValue("ABC"), new TextValue("abc")))
        .isEqualTo(new BooleanValue(false));
  }

  @Test
  void search() {
    assertThat(call("SEARCH", new TextValue("o"), new TextValue("Hello World")))
        .isEqualTo(new NumberValue(5.0));
    assertThat(call("SEARCH", new TextValue("O"), new TextValue("Hello World")))
        .isEqualTo(new NumberValue(5.0));
    assertThat(call("SEARCH", new TextValue("x"), new TextValue("Hello")))
        .isInstanceOf(ErrorValue.class);
  }

  @Test
  void replace() {
    assertThat(call("REPLACE", new TextValue("abcdef"), new NumberValue(2), new NumberValue(3), new TextValue("XY")))
        .isEqualTo(new TextValue("aXYef"));
  }

  @Test
  void charFunc() {
    assertThat(call("CHAR", new NumberValue(65)))
        .isEqualTo(new TextValue("A"));
  }

  @Test
  void code() {
    assertThat(call("CODE", new TextValue("A")))
        .isEqualTo(new NumberValue(65.0));
  }

  @Test
  void value() {
    assertThat(call("VALUE", new TextValue("42")))
        .isEqualTo(new NumberValue(42.0));
    assertThat(call("VALUE", new TextValue("3.14")))
        .isEqualTo(new NumberValue(3.14));
    assertThat(call("VALUE", new TextValue("abc")))
        .isInstanceOf(ErrorValue.class);
  }

  @Test
  void t() {
    assertThat(call("T", new TextValue("hello")))
        .isEqualTo(new TextValue("hello"));
    assertThat(call("T", new NumberValue(42)))
        .isEqualTo(new TextValue(""));
  }
}
