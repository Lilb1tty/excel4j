package io.github.lilb1tty.cellix.formula.functions;

import io.github.lilb1tty.cellix.core.model.*;
import io.github.lilb1tty.cellix.formula.EvalContext;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class LogicFunctionsExtTest {

    private static final EvalContext CTX = new EvalContext() {
        @Override public CellValue resolve(CellRef ref) { return new BlankValue(); }
        @Override public CellValue resolve(WorksheetName sheet, CellRef ref) { return new BlankValue(); }
    };

    private CellValue call(String name, CellValue... args) {
        return new io.github.lilb1tty.cellix.formula.FunctionRegistry().find(name).evaluate(List.of(args), CTX);
    }

    @Test
    void xor() {
        assertThat(call("XOR", new BooleanValue(true), new BooleanValue(false))).isEqualTo(new BooleanValue(true));
        assertThat(call("XOR", new BooleanValue(true), new BooleanValue(true))).isEqualTo(new BooleanValue(false));
        assertThat(call("XOR", new BooleanValue(false), new BooleanValue(false))).isEqualTo(new BooleanValue(false));
    }

    @Test
    void ifs() {
        assertThat(call("IFS", new BooleanValue(false), new TextValue("a"), new BooleanValue(true), new TextValue("b")))
            .isEqualTo(new TextValue("b"));
    }

    @Test
    void ifsNoMatch() {
        assertThat(call("IFS", new BooleanValue(false), new TextValue("a")))
            .isInstanceOf(ErrorValue.class);
    }

    @Test
    void switchFunc() {
        assertThat(call("SWITCH", new NumberValue(2), new NumberValue(1), new TextValue("one"),
                new NumberValue(2), new TextValue("two"), new TextValue("other")))
            .isEqualTo(new TextValue("two"));
        assertThat(call("SWITCH", new NumberValue(9), new NumberValue(1), new TextValue("one"),
                new TextValue("other")))
            .isEqualTo(new TextValue("other"));
    }

    @Test
    void ifna() {
        assertThat(call("IFNA", new ErrorValue(ErrorType.NA), new TextValue("missing")))
            .isEqualTo(new TextValue("missing"));
        assertThat(call("IFNA", new TextValue("ok"), new TextValue("missing")))
            .isEqualTo(new TextValue("ok"));
        assertThat(call("IFNA", new ErrorValue(ErrorType.DIV_BY_ZERO), new TextValue("missing")))
            .isInstanceOf(ErrorValue.class);
    }
}
