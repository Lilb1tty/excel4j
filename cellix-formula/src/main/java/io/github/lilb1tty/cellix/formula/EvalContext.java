package io.github.lilb1tty.cellix.formula;

import io.github.lilb1tty.cellix.core.model.CellRef;
import io.github.lilb1tty.cellix.core.model.CellValue;
import io.github.lilb1tty.cellix.core.model.WorksheetName;

public interface EvalContext {
    CellValue resolve(CellRef ref);

    CellValue resolve(WorksheetName sheet, CellRef ref);
}
