package io.github.lilb1tty.excel4j.formula.ast;

import io.github.lilb1tty.excel4j.core.model.ErrorType;

public record ErrorLiteral(ErrorType type) implements FormulaNode {}
