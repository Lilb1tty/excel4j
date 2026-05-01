package io.excel4j.formula.ast;

public sealed interface FormulaNode
    permits NumberLiteral, TextLiteral, BoolLiteral, ErrorLiteral,
            CellRefNode, CellRangeNode,
            BinaryOp, UnaryOp,
            FunctionCall, NameRef {}
