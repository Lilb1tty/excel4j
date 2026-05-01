package io.excel4j.formula.ast;

import io.excel4j.core.model.CellRef;

public record CellRefNode(CellRef ref, boolean rowAbsolute, boolean colAbsolute)
    implements FormulaNode {}
