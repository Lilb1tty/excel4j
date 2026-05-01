package io.excel4j.formula.ast;

import io.excel4j.core.model.ErrorType;

public record ErrorLiteral(ErrorType type) implements FormulaNode {}
