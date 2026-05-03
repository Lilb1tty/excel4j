package io.github.lilb1tty.excel4j.formula.ast;

import java.util.List;

public record FunctionCall(String name, List<FormulaNode> args) implements FormulaNode {}
