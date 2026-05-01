package io.excel4j.formula;

import io.excel4j.formula.ast.*;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class ParserTest {

    @Test
    void parseNumber() {
        FormulaNode node = new Parser().parse("42");
        assertThat(node).isInstanceOf(NumberLiteral.class);
        assertThat(((NumberLiteral) node).value()).isEqualTo(42.0);
    }

    @Test
    void parseAddition() {
        FormulaNode node = new Parser().parse("1+2");
        assertThat(node).isInstanceOf(BinaryOp.class);
        BinaryOp op = (BinaryOp) node;
        assertThat(op.op()).isEqualTo(Operator.ADD);
        assertThat(((NumberLiteral) op.left()).value()).isEqualTo(1.0);
        assertThat(((NumberLiteral) op.right()).value()).isEqualTo(2.0);
    }

    @Test
    void parsePrecedence() {
        FormulaNode node = new Parser().parse("1+2*3");
        BinaryOp op = (BinaryOp) node;
        assertThat(op.op()).isEqualTo(Operator.ADD);
        assertThat(((NumberLiteral) op.left()).value()).isEqualTo(1.0);
        BinaryOp right = (BinaryOp) op.right();
        assertThat(right.op()).isEqualTo(Operator.MULTIPLY);
    }

    @Test
    void parseCellRef() {
        FormulaNode node = new Parser().parse("A1");
        assertThat(node).isInstanceOf(CellRefNode.class);
        CellRefNode ref = (CellRefNode) node;
        assertThat(ref.ref().row()).isEqualTo(1);
        assertThat(ref.ref().col()).isEqualTo(1);
    }

    @Test
    void parseCellRange() {
        FormulaNode node = new Parser().parse("A1:B10");
        assertThat(node).isInstanceOf(CellRangeNode.class);
    }

    @Test
    void parseFunctionCall() {
        FormulaNode node = new Parser().parse("SUM(A1,A2)");
        FunctionCall call = (FunctionCall) node;
        assertThat(call.name()).isEqualTo("SUM");
        assertThat(call.args()).hasSize(2);
    }

    @Test
    void parseUnaryMinus() {
        FormulaNode node = new Parser().parse("-5");
        UnaryOp op = (UnaryOp) node;
        assertThat(op.op()).isEqualTo(UnaryOperator.NEGATE);
    }
}
