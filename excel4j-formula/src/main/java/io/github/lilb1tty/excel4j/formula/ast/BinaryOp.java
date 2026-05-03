package io.github.lilb1tty.excel4j.formula.ast;

public record BinaryOp(Operator op, FormulaNode left, FormulaNode right) implements FormulaNode {}
