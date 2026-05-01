package io.excel4j.formula.ast;

public enum Operator {
    ADD, SUBTRACT, MULTIPLY, DIVIDE, POWER, CONCAT,
    EQ, NE, LT, GT, LE, GE;

    public int precedence() {
        return switch (this) {
            case EQ, NE, LT, GT, LE, GE -> 1;
            case CONCAT -> 2;
            case ADD, SUBTRACT -> 3;
            case MULTIPLY, DIVIDE -> 4;
            case POWER -> 5;
        };
    }
}
