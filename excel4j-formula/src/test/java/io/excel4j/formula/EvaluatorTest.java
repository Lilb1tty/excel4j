package io.excel4j.formula;

import io.excel4j.core.model.*;
import io.excel4j.formula.ast.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class EvaluatorTest {

    EvalContext dummyCtx = new EvalContext() {
        @Override
        public CellValue resolve(CellRef ref) {
            return new BlankValue();
        }

        @Override
        public CellValue resolve(WorksheetName sheet, CellRef ref) {
            return new BlankValue();
        }
    };

    @Test
    void evaluateNumber() {
        Evaluator ev = new Evaluator(dummyCtx);
        assertThat(ev.evaluate(new NumberLiteral(42.0)))
                .isEqualTo(new NumberValue(42.0));
    }

    @Test
    void evaluateAddition() {
        Evaluator ev = new Evaluator(dummyCtx);
        FormulaNode node = new BinaryOp(Operator.ADD,
                new NumberLiteral(1.0), new NumberLiteral(2.0));
        assertThat(ev.evaluate(node))
                .isEqualTo(new NumberValue(3.0));
    }

    @Test
    void evaluateConcatenation() {
        Evaluator ev = new Evaluator(dummyCtx);
        FormulaNode node = new BinaryOp(Operator.CONCAT,
                new TextLiteral("Hello"), new TextLiteral("World"));
        assertThat(ev.evaluate(node))
                .isEqualTo(new TextValue("HelloWorld"));
    }

    @Test
    void evaluateDivisionByZero() {
        Evaluator ev = new Evaluator(dummyCtx);
        FormulaNode node = new BinaryOp(Operator.DIVIDE,
                new NumberLiteral(1.0), new NumberLiteral(0.0));
        assertThat(ev.evaluate(node))
                .isEqualTo(new ErrorValue(ErrorType.DIV_BY_ZERO));
    }

    @Test
    void evaluateUnaryNegate() {
        Evaluator ev = new Evaluator(dummyCtx);
        FormulaNode node = new UnaryOp(UnaryOperator.NEGATE, new NumberLiteral(5.0));
        assertThat(ev.evaluate(node))
                .isEqualTo(new NumberValue(-5.0));
    }
}
