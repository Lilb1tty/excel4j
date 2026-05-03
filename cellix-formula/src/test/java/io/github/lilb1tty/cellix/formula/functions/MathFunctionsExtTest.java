package io.github.lilb1tty.cellix.formula.functions;

import io.github.lilb1tty.cellix.core.model.*;
import io.github.lilb1tty.cellix.formula.EvalContext;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class MathFunctionsExtTest {

    private static final EvalContext CTX = new EvalContext() {
        @Override public CellValue resolve(CellRef ref) { return new BlankValue(); }
        @Override public CellValue resolve(WorksheetName sheet, CellRef ref) { return new BlankValue(); }
    };

    private CellValue call(String name, CellValue... args) {
        var reg = new io.github.lilb1tty.cellix.formula.FunctionRegistry();
        return reg.find(name).evaluate(List.of(args), CTX);
    }

    @Test void ceiling() {
        assertThat(call("CEILING", new NumberValue(2.3), new NumberValue(1))).isEqualTo(new NumberValue(3.0));
        assertThat(call("CEILING", new NumberValue(2.3), new NumberValue(0.5))).isEqualTo(new NumberValue(2.5));
    }
    @Test void floor() {
        assertThat(call("FLOOR", new NumberValue(2.7), new NumberValue(1))).isEqualTo(new NumberValue(2.0));
        assertThat(call("FLOOR", new NumberValue(2.7), new NumberValue(0.5))).isEqualTo(new NumberValue(2.5));
    }
    @Test void roundUp() {
        assertThat(call("ROUNDUP", new NumberValue(2.1), new NumberValue(0))).isEqualTo(new NumberValue(3.0));
        assertThat(call("ROUNDUP", new NumberValue(-2.1), new NumberValue(0))).isEqualTo(new NumberValue(-3.0));
    }
    @Test void roundDown() {
        assertThat(call("ROUNDDOWN", new NumberValue(2.9), new NumberValue(0))).isEqualTo(new NumberValue(2.0));
        assertThat(call("ROUNDDOWN", new NumberValue(-2.9), new NumberValue(0))).isEqualTo(new NumberValue(-2.0));
    }
    @Test void trunc() {
        assertThat(call("TRUNC", new NumberValue(3.7))).isEqualTo(new NumberValue(3.0));
        assertThat(call("TRUNC", new NumberValue(-3.7))).isEqualTo(new NumberValue(-3.0));
        assertThat(call("TRUNC", new NumberValue(3.789), new NumberValue(2))).isEqualTo(new NumberValue(3.78));
    }
    @Test void sign() {
        assertThat(call("SIGN", new NumberValue(5))).isEqualTo(new NumberValue(1.0));
        assertThat(call("SIGN", new NumberValue(0))).isEqualTo(new NumberValue(0.0));
        assertThat(call("SIGN", new NumberValue(-5))).isEqualTo(new NumberValue(-1.0));
    }
    @Test void log() {
        assertThat(((NumberValue) call("LOG", new NumberValue(100), new NumberValue(10))).value())
            .isCloseTo(2.0, within(1e-10));
        assertThat(((NumberValue) call("LOG", new NumberValue(10))).value())
            .isCloseTo(1.0, within(1e-10));
    }
    @Test void log10() {
        assertThat(((NumberValue) call("LOG10", new NumberValue(1000))).value()).isCloseTo(3.0, within(1e-10));
    }
    @Test void ln() {
        assertThat(((NumberValue) call("LN", new NumberValue(Math.E))).value()).isCloseTo(1.0, within(1e-10));
    }
    @Test void exp() {
        assertThat(((NumberValue) call("EXP", new NumberValue(1))).value()).isCloseTo(Math.E, within(1e-10));
    }
    @Test void pi() {
        assertThat(((NumberValue) call("PI")).value()).isCloseTo(Math.PI, within(1e-10));
    }
    @Test void rand() {
        var v = (NumberValue) call("RAND");
        assertThat(v.value()).isBetween(0.0, 1.0);
    }
    @Test void randBetween() {
        var v = (NumberValue) call("RANDBETWEEN", new NumberValue(1), new NumberValue(10));
        assertThat(v.value()).isBetween(1.0, 10.0);
    }
    @Test void fact() {
        assertThat(call("FACT", new NumberValue(5))).isEqualTo(new NumberValue(120.0));
        assertThat(call("FACT", new NumberValue(0))).isEqualTo(new NumberValue(1.0));
    }
    @Test void sumProduct() {
        assertThat(call("SUMPRODUCT", new NumberValue(2), new NumberValue(3))).isEqualTo(new NumberValue(5.0));
    }
    @Test void sumSq() {
        assertThat(call("SUMSQ", new NumberValue(3), new NumberValue(4))).isEqualTo(new NumberValue(25.0));
    }
}
