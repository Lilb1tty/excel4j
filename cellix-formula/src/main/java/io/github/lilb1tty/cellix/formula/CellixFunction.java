package io.github.lilb1tty.cellix.formula;

import io.github.lilb1tty.cellix.core.model.CellValue;
import java.util.List;

public interface CellixFunction {
  CellValue evaluate(List<CellValue> args, EvalContext ctx);
}
