package io.excel4j.formula.token;

public enum TokenType {
    NUMBER, TEXT, BOOL, ERROR,
    CELL_REF,
    NAME,
    PLUS, MINUS, MULTIPLY, DIVIDE, POWER, CONCAT,
    EQ, NE, LT, GT, LE, GE,
    LPAREN, RPAREN, COMMA, COLON, PERCENT,
    EOF
}
