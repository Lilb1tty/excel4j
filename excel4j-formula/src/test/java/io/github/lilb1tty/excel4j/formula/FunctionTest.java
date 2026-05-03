package io.github.lilb1tty.excel4j.formula;

import io.github.lilb1tty.excel4j.core.model.*;
import io.github.lilb1tty.excel4j.formula.ast.*;
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

    @Test
    void ifFunction() {
        assertThat(eval("IF(TRUE,1,2)")).isEqualTo(new NumberValue(1.0));
        assertThat(eval("IF(FALSE,1,2)")).isEqualTo(new NumberValue(2.0));
    }

    @Test
    void andOrNot() {
        assertThat(eval("AND(TRUE,TRUE)")).isEqualTo(new BooleanValue(true));
        assertThat(eval("AND(TRUE,FALSE)")).isEqualTo(new BooleanValue(false));
        assertThat(eval("OR(FALSE,TRUE)")).isEqualTo(new BooleanValue(true));
        assertThat(eval("NOT(TRUE)")).isEqualTo(new BooleanValue(false));
    }

    @Test
    void ifErrorFunction() {
        assertThat(eval("IFERROR(1/0,99)")).isEqualTo(new NumberValue(99.0));
        assertThat(eval("IFERROR(42,99)")).isEqualTo(new NumberValue(42.0));
    }

    @Test
    void isFunctions() {
        assertThat(eval("ISNUMBER(42)")).isEqualTo(new BooleanValue(true));
        assertThat(eval("ISTEXT(\"hello\")")).isEqualTo(new BooleanValue(true));
        assertThat(eval("ISERROR(1/0)")).isEqualTo(new BooleanValue(true));
    }

    @Test
    void leftRightMid() {
        assertThat(eval("LEFT(\"hello\",2)")).isEqualTo(new TextValue("he"));
        assertThat(eval("RIGHT(\"hello\",2)")).isEqualTo(new TextValue("lo"));
        assertThat(eval("MID(\"hello\",2,2)")).isEqualTo(new TextValue("el"));
    }

    @Test
    void lenAndTrim() {
        assertThat(eval("LEN(\"hello\")")).isEqualTo(new NumberValue(5.0));
        assertThat(eval("TRIM(\"  hello  \")")).isEqualTo(new TextValue("hello"));
    }

    @Test
    void concatenateFunction() {
        assertThat(eval("CONCATENATE(\"He\",\"llo\")"))
            .isEqualTo(new TextValue("Hello"));
    }

    @Test
    void upperLower() {
        assertThat(eval("UPPER(\"hello\")")).isEqualTo(new TextValue("HELLO"));
        assertThat(eval("LOWER(\"HELLO\")")).isEqualTo(new TextValue("hello"));
    }

    @Test
    void findFunction() {
        assertThat(eval("FIND(\"el\",\"hello\")")).isEqualTo(new NumberValue(2.0));
    }

    @Test
    void yearMonthDay() {
        assertThat(eval("YEAR(DATE(2024,1,15))")).isEqualTo(new NumberValue(2024.0));
        assertThat(eval("MONTH(DATE(2024,1,15))")).isEqualTo(new NumberValue(1.0));
        assertThat(eval("DAY(DATE(2024,1,15))")).isEqualTo(new NumberValue(15.0));
    }

    @Test
    void weekdayFunction() {
        assertThat(eval("WEEKDAY(DATE(2024,1,15),2)")).isInstanceOf(NumberValue.class);
    }
}
