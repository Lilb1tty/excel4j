package io.github.lilb1tty.excel4j.formula.ast;

import io.github.lilb1tty.excel4j.core.model.CellRange;

public record CellRangeNode(CellRange range) implements FormulaNode {}
