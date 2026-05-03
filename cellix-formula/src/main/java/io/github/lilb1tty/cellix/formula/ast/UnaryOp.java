package io.github.lilb1tty.cellix.formula.ast;

public record UnaryOp(UnaryOperator op, FormulaNode operand) implements FormulaNode {}
