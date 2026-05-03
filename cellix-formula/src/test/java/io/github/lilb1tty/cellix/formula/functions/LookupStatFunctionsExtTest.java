package io.github.lilb1tty.cellix.formula.functions;

import io.github.lilb1tty.cellix.core.model.*;
import io.github.lilb1tty.cellix.formula.EvalContext;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class LookupStatFunctionsExtTest {

    private static final EvalContext CTX = new EvalContext() {
        @Override public CellValue resolve(CellRef ref) { return new BlankValue(); }
        @Override public CellValue resolve(WorksheetName sheet, CellRef ref) { return new BlankValue(); }
    };

    private CellValue call(String name, CellValue... args) {
        var reg = new io.github.lilb1tty.cellix.formula.FunctionRegistry();
        LookupStatFunctions.registerAll(reg);
        return reg.find(name).evaluate(List.of(args), CTX);
    }

    @Test void choose() {
        assertThat(call("CHOOSE", new NumberValue(2), new TextValue("a"), new TextValue("b"), new TextValue("c")))
            .isEqualTo(new TextValue("b"));
    }
    @Test void large() {
        // LARGE(values..., k) — last arg is k
        // LARGE(5, 3, 1, 4, 2, k=1) → largest = 5
        assertThat(call("LARGE", new NumberValue(5), new NumberValue(3), new NumberValue(1),
                new NumberValue(4), new NumberValue(2), new NumberValue(1)))
            .isEqualTo(new NumberValue(5.0));
        // LARGE(5, 3, 1, 4, 2, k=3) → 3rd largest = 3
        assertThat(call("LARGE", new NumberValue(5), new NumberValue(3), new NumberValue(1),
                new NumberValue(4), new NumberValue(2), new NumberValue(3)))
            .isEqualTo(new NumberValue(3.0));
    }
    @Test void small() {
        // SMALL(values..., k) — last arg is k
        assertThat(call("SMALL", new NumberValue(3), new NumberValue(1), new NumberValue(2), new NumberValue(1)))
            .isEqualTo(new NumberValue(1.0));
    }
    @Test void rank() {
        // RANK(number, values...) — number is first, rest are the list, descending by default
        // RANK(3, 5, 3, 1) → rank 2 in descending
        assertThat(call("RANK", new NumberValue(3), new NumberValue(5), new NumberValue(3), new NumberValue(1)))
            .isEqualTo(new NumberValue(2.0));
    }
    @Test void median() {
        assertThat(call("MEDIAN", new NumberValue(1), new NumberValue(3), new NumberValue(2)))
            .isEqualTo(new NumberValue(2.0));
        assertThat(call("MEDIAN", new NumberValue(1), new NumberValue(2), new NumberValue(3), new NumberValue(4)))
            .isEqualTo(new NumberValue(2.5));
    }
    @Test void mode() {
        assertThat(call("MODE", new NumberValue(1), new NumberValue(2), new NumberValue(2), new NumberValue(3)))
            .isEqualTo(new NumberValue(2.0));
    }
    @Test void stdev() {
        var result = (NumberValue) call("STDEV", new NumberValue(2), new NumberValue(4), new NumberValue(4),
                new NumberValue(4), new NumberValue(5), new NumberValue(5), new NumberValue(7), new NumberValue(9));
        assertThat(result.value()).isCloseTo(2.138, within(0.001));
    }
    @Test void var() {
        var result = (NumberValue) call("VAR", new NumberValue(2), new NumberValue(4), new NumberValue(4),
                new NumberValue(4), new NumberValue(5), new NumberValue(5), new NumberValue(7), new NumberValue(9));
        assertThat(result.value()).isCloseTo(4.571, within(0.001));
    }
}
