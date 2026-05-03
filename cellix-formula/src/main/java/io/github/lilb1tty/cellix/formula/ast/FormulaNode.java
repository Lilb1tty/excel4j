package io.github.lilb1tty.cellix.formula.ast;

public sealed interface FormulaNode
    permits NumberLiteral, TextLiteral, BoolLiteral, ErrorLiteral,
            CellRefNode, CellRangeNode,
            BinaryOp, UnaryOp,
            FunctionCall, NameRef, ArrayLiteral {}
