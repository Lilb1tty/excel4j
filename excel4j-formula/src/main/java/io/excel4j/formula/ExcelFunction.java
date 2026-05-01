package io.excel4j.formula;

import io.excel4j.core.model.CellValue;
import java.util.List;

public interface ExcelFunction {
  CellValue evaluate(List<CellValue> args, EvalContext ctx);
}
