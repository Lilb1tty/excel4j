package io.github.lilb1tty.cellix.formula.ast;

import io.github.lilb1tty.cellix.core.model.ErrorType;

public record ErrorLiteral(ErrorType type) implements FormulaNode {}
