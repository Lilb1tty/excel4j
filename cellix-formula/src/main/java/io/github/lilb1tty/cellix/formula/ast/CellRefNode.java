package io.github.lilb1tty.cellix.formula.ast;

import io.github.lilb1tty.cellix.core.model.CellRef;

public record CellRefNode(CellRef ref, boolean rowAbsolute, boolean colAbsolute)
    implements FormulaNode {}
