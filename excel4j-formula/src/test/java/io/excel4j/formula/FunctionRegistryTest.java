package io.excel4j.formula;

import io.excel4j.core.model.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class FunctionRegistryTest {

  @Test
  void findRegisteredFunction() {
    FunctionRegistry reg = new FunctionRegistry();
    reg.register("SUM", (args, ctx) -> new NumberValue(0.0));
    ExcelFunction fn = reg.find("SUM");
    assertThat(fn).isNotNull();
  }

  @Test
  void findUnknownFunction() {
    FunctionRegistry reg = new FunctionRegistry();
    assertThat(reg.find("NONEXISTENT")).isNull();
  }

  @Test
  void registerCustomFunction() {
    FunctionRegistry reg = new FunctionRegistry();
    reg.register("DOUBLE", (args, ctx) -> new NumberValue(
        ((NumberValue) args.get(0)).value() * 2));
    ExcelFunction fn = reg.find("DOUBLE");
    assertThat(fn.evaluate(List.of(new NumberValue(5.0)), null))
        .isEqualTo(new NumberValue(10.0));
  }
}
