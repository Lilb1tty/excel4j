package io.excel4j.formula.functions;

import io.excel4j.core.model.*;
import io.excel4j.formula.EvalContext;
import io.excel4j.formula.FormulaEvaluator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ArrayFormulaTest {

    private static final EvalContext CTX = new EvalContext() {
        @Override
        public CellValue resolve(CellRef ref) {
            return new BlankValue();
        }

        @Override
        public CellValue resolve(io.excel4j.core.model.WorksheetName sheet, CellRef ref) {
            return new BlankValue();
        }
    };

    private CellValue eval(String formula) {
        return new FormulaEvaluator().evaluate(formula, CTX);
    }

    @Test
    void arrayLiteralRowVector() {
        var result = eval("{1,2,3}");
        assertThat(result).isInstanceOf(RangeValue.class);
        var rv = (RangeValue) result;
        assertThat(rv.values()).containsExactly(
            new NumberValue(1), new NumberValue(2), new NumberValue(3));
    }

    @Test
    void arrayLiteralColumnVector() {
        var result = eval("{1;2;3}");
        assertThat(result).isInstanceOf(RangeValue.class);
        var rv = (RangeValue) result;
        assertThat(rv.values()).containsExactly(
            new NumberValue(1), new NumberValue(2), new NumberValue(3));
    }

    @Test
    void arrayLiteralMatrix() {
        var result = eval("{1,2;3,4;5,6}");
        assertThat(result).isInstanceOf(RangeValue.class);
        var rv = (RangeValue) result;
        assertThat(rv.values()).containsExactly(
            new NumberValue(1), new NumberValue(2),
            new NumberValue(3), new NumberValue(4),
            new NumberValue(5), new NumberValue(6));
    }

    @Test
    void arrayLiteralWithExpressions() {
        var result = eval("{1+1,2*3}");
        assertThat(result).isInstanceOf(RangeValue.class);
        var rv = (RangeValue) result;
        assertThat(rv.values()).containsExactly(
            new NumberValue(2), new NumberValue(6));
    }

    @Test
    void sumOfArrayLiteral() {
        var result = eval("SUM({1,2,3})");
        assertThat(result).isEqualTo(new NumberValue(6));
    }

    @Test
    void sequenceDefault() {
        var result = eval("SEQUENCE(3)");
        assertThat(result).isInstanceOf(RangeValue.class);
        var rv = (RangeValue) result;
        assertThat(rv.values()).containsExactly(
            new NumberValue(1), new NumberValue(2), new NumberValue(3));
    }

    @Test
    void sequenceRowsAndCols() {
        var result = eval("SEQUENCE(2,3)");
        assertThat(result).isInstanceOf(RangeValue.class);
        var rv = (RangeValue) result;
        assertThat(rv.values()).containsExactly(
            new NumberValue(1), new NumberValue(2), new NumberValue(3),
            new NumberValue(4), new NumberValue(5), new NumberValue(6));
    }

    @Test
    void sequenceWithStartAndStep() {
        var result = eval("SEQUENCE(3,1,10,5)");
        assertThat(result).isInstanceOf(RangeValue.class);
        var rv = (RangeValue) result;
        assertThat(rv.values()).containsExactly(
            new NumberValue(10), new NumberValue(15), new NumberValue(20));
    }

    @Test
    void sumOfSequence() {
        var result = eval("SUM(SEQUENCE(3,3))");
        assertThat(result).isEqualTo(new NumberValue(45)); // 1+2+...+9
    }

    @Test
    void uniqueFlatValues() {
        var result = eval("UNIQUE(1,2,2,3,1)");
        assertThat(result).isInstanceOf(RangeValue.class);
        var rv = (RangeValue) result;
        assertThat(rv.values()).containsExactly(
            new NumberValue(1), new NumberValue(2), new NumberValue(3));
    }

    @Test
    void sortFlatValues() {
        var result = eval("SORT(3,1,2)");
        assertThat(result).isInstanceOf(RangeValue.class);
        var rv = (RangeValue) result;
        assertThat(rv.values()).containsExactly(
            new NumberValue(1), new NumberValue(2), new NumberValue(3));
    }
}
