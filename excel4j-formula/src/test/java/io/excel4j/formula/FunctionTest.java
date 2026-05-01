package io.excel4j.formula;

import io.excel4j.core.model.*;
import io.excel4j.formula.ast.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class FunctionTest {

    EvalContext dummyCtx = new EvalContext() {
        @Override public CellValue resolve(CellRef ref) { return new BlankValue(); }
        @Override public CellValue resolve(WorksheetName sheet, CellRef ref) { return new BlankValue(); }
    };

    CellValue eval(String formula) {
        FormulaNode node = new Parser().parse(formula);
        return new Evaluator(dummyCtx).evaluate(node);
    }

    @Test
    void sumFunction() {
        assertThat(eval("SUM(1,2,3)")).isEqualTo(new NumberValue(6.0));
    }

    @Test
    void averageFunction() {
        assertThat(eval("AVERAGE(2,4,6)")).isEqualTo(new NumberValue(4.0));
    }

    @Test
    void minMax() {
        assertThat(eval("MIN(5,1,3)")).isEqualTo(new NumberValue(1.0));
        assertThat(eval("MAX(5,1,3)")).isEqualTo(new NumberValue(5.0));
    }

    @Test
    void roundFunction() {
        assertThat(eval("ROUND(3.14159,2)")).isEqualTo(new NumberValue(3.14));
    }

    @Test
    void modFunction() {
        assertThat(eval("MOD(10,3)")).isEqualTo(new NumberValue(1.0));
    }

    @Test
    void sqrtNegative() {
        assertThat(eval("SQRT(-1)")).isEqualTo(new ErrorValue(ErrorType.NUM));
    }
}
