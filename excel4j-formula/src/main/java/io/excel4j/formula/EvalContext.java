package io.excel4j.formula;

import io.excel4j.core.model.CellRef;
import io.excel4j.core.model.CellValue;
import io.excel4j.core.model.WorksheetName;

public interface EvalContext {
    CellValue resolve(CellRef ref);

    CellValue resolve(WorksheetName sheet, CellRef ref);
}
