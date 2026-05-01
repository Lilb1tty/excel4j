package io.excel4j.formula.functions;

import io.excel4j.core.model.CellValue;
import io.excel4j.formula.EvalContext;

import java.util.List;

public interface ExcelFunction {
    CellValue evaluate(List<CellValue> args, EvalContext ctx);
}
