package io.github.lilb1tty.excel4j.formula.ast;

import io.github.lilb1tty.excel4j.core.model.CellRef;

public record CellRefNode(CellRef ref, boolean rowAbsolute, boolean colAbsolute)
    implements FormulaNode {}
