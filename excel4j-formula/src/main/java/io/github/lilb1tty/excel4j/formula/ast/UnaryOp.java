package io.github.lilb1tty.excel4j.formula.ast;

public record UnaryOp(UnaryOperator op, FormulaNode operand) implements FormulaNode {}
