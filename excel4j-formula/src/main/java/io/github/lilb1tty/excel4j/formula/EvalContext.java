package io.github.lilb1tty.excel4j.formula;

import io.github.lilb1tty.excel4j.core.model.CellRef;
import io.github.lilb1tty.excel4j.core.model.CellValue;
import io.github.lilb1tty.excel4j.core.model.WorksheetName;

public interface EvalContext {
    CellValue resolve(CellRef ref);

    CellValue resolve(WorksheetName sheet, CellRef ref);
}
