package io.excel4j.formula.token;

public record Token(TokenType type, String text, int pos) {}
