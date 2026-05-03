package io.github.lilb1tty.cellix.formula.ast;

public record BinaryOp(Operator op, FormulaNode left, FormulaNode right) implements FormulaNode {}
