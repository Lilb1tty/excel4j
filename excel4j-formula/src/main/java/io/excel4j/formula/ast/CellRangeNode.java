package io.excel4j.formula.ast;

import io.excel4j.core.model.CellRange;

public record CellRangeNode(CellRange range) implements FormulaNode {}
