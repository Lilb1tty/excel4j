package io.excel4j.formula.ast;

public record UnaryOp(UnaryOperator op, FormulaNode operand) implements FormulaNode {}
