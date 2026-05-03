package io.github.lilb1tty.cellix.formula.ast;

import java.util.List;

public record FunctionCall(String name, List<FormulaNode> args) implements FormulaNode {}
