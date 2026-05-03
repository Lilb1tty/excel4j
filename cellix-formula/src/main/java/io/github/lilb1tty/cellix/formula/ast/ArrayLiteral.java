package io.github.lilb1tty.cellix.formula.ast;

import java.util.List;

public record ArrayLiteral(List<List<FormulaNode>> rows) implements FormulaNode {}
