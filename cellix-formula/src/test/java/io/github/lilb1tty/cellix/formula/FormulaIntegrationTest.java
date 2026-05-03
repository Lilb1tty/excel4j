package io.github.lilb1tty.cellix.formula;

import io.github.lilb1tty.cellix.core.model.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class FormulaIntegrationTest {

    @Test
    void evaluateSumFormula() {
        Workbook wb = new Workbook();
        wb.sheet(1).cell("A1").setValue(new NumberValue(10.0));
        wb.sheet(1).cell("A2").setValue(new NumberValue(20.0));
        wb.sheet(1).cell("A3").setFormula("SUM(A1:A2)");

        wb.recalculate(new FormulaEvaluator().workbookEvaluator(wb));

        assertThat(wb.sheet(1).cell("A3").getCachedValue())
            .isEqualTo(new NumberValue(30.0));
    }

    @Test
    void evaluateIfFormula() {
        Workbook wb = new Workbook();
        wb.sheet(1).cell("A1").setValue(new NumberValue(5.0));
        wb.sheet(1).cell("A2").setFormula("IF(A1>3,\"yes\",\"no\")");

        wb.recalculate(new FormulaEvaluator().workbookEvaluator(wb));

        assertThat(wb.sheet(1).cell("A2").getCachedValue())
            .isEqualTo(new TextValue("yes"));
    }

    @Test
    void evaluateNestedFormula() {
        Workbook wb = new Workbook();
        wb.sheet(1).cell("A1").setValue(new NumberValue(2.0));
        wb.sheet(1).cell("A2").setValue(new NumberValue(3.0));
        wb.sheet(1).cell("A3").setFormula("SUM(A1,A2)*2");

        wb.recalculate(new FormulaEvaluator().workbookEvaluator(wb));

        assertThat(wb.sheet(1).cell("A3").getCachedValue())
            .isEqualTo(new NumberValue(10.0));
    }

    @Test
    void circularRefInWorkbook() {
        Workbook wb = new Workbook();
        wb.sheet(1).cell("A1").setFormula("A1+1");

        wb.recalculate(new FormulaEvaluator().workbookEvaluator(wb));

        assertThat(wb.sheet(1).cell("A1").getCachedValue())
            .isEqualTo(new ErrorValue(ErrorType.CIRCULAR_REF));
    }

    @Test
    void formulaWithTextFunction() {
        Workbook wb = new Workbook();
        wb.sheet(1).cell("A1").setValue(new TextValue("hello"));
        wb.sheet(1).cell("A2").setFormula("UPPER(A1)");

        wb.recalculate(new FormulaEvaluator().workbookEvaluator(wb));

        assertThat(wb.sheet(1).cell("A2").getCachedValue())
            .isEqualTo(new TextValue("HELLO"));
    }
}
