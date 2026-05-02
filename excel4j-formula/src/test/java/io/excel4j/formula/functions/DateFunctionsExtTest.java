package io.excel4j.formula.functions;

import io.excel4j.core.model.*;
import io.excel4j.formula.EvalContext;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class DateFunctionsExtTest {

    private static final EvalContext CTX = new EvalContext() {
        @Override public CellValue resolve(CellRef ref) { return new BlankValue(); }
        @Override public CellValue resolve(WorksheetName sheet, CellRef ref) { return new BlankValue(); }
    };

    private CellValue call(String name, CellValue... args) {
        return new io.excel4j.formula.FunctionRegistry().find(name).evaluate(List.of(args), CTX);
    }

    @Test
    void edate() {
        assertThat(call("EDATE", new DateValue(LocalDate.of(2024, 1, 15)), new NumberValue(2)))
            .isEqualTo(new DateValue(LocalDate.of(2024, 3, 15)));
        assertThat(call("EDATE", new DateValue(LocalDate.of(2024, 1, 15)), new NumberValue(-1)))
            .isEqualTo(new DateValue(LocalDate.of(2023, 12, 15)));
    }

    @Test
    void eomonth() {
        assertThat(call("EOMONTH", new DateValue(LocalDate.of(2024, 1, 10)), new NumberValue(0)))
            .isEqualTo(new DateValue(LocalDate.of(2024, 1, 31)));
        assertThat(call("EOMONTH", new DateValue(LocalDate.of(2024, 1, 10)), new NumberValue(1)))
            .isEqualTo(new DateValue(LocalDate.of(2024, 2, 29)));
    }

    @Test
    void days() {
        assertThat(call("DAYS", new DateValue(LocalDate.of(2024, 1, 10)), new DateValue(LocalDate.of(2024, 1, 1))))
            .isEqualTo(new NumberValue(9.0));
    }

    @Test
    void time() {
        var result = call("TIME", new NumberValue(14), new NumberValue(30), new NumberValue(0));
        assertThat(result).isInstanceOf(DateTimeValue.class);
    }

    @Test
    void datedif() {
        var start = new DateValue(LocalDate.of(2020, 1, 1));
        var end   = new DateValue(LocalDate.of(2024, 6, 15));
        assertThat(call("DATEDIF", start, end, new TextValue("Y"))).isEqualTo(new NumberValue(4.0));
        assertThat(call("DATEDIF", start, end, new TextValue("M"))).isEqualTo(new NumberValue(53.0));
        assertThat(((NumberValue) call("DATEDIF", start, end, new TextValue("D"))).value()).isGreaterThan(0);
    }

    @Test
    void isoWeekNum() {
        assertThat(call("ISOWEEKNUM", new DateValue(LocalDate.of(2024, 1, 1)))).isEqualTo(new NumberValue(1.0));
    }
}
