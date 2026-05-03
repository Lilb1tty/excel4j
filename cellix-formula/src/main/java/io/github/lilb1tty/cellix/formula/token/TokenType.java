package io.github.lilb1tty.cellix.formula.token;

public enum TokenType {
    NUMBER, TEXT, BOOL, ERROR,
    CELL_REF,
    NAME,
    PLUS, MINUS, MULTIPLY, DIVIDE, POWER, CONCAT,
    EQ, NE, LT, GT, LE, GE,
    LPAREN, RPAREN, COMMA, COLON, PERCENT,
    LBRACE, RBRACE, SEMICOLON,
    EOF
}
