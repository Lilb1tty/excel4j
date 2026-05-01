package io.excel4j.core.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.*;

class CellValueTest {

    @Test
    void patternMatchTextValue() {
        CellValue value = new TextValue("hello");
        String result = switch (value) {
            case TextValue(var s) -> s;
            default -> "other";
        };
        assertThat(result).isEqualTo("hello");
    }

    @Test
    void patternMatchNumberValue() {
        CellValue value = new NumberValue(42.5);
        double result = switch (value) {
            case NumberValue(var d) -> d;
            default -> 0.0;
        };
        assertThat(result).isEqualTo(42.5);
    }

    @Test
    void patternMatchDateValue() {
        LocalDate date = LocalDate.of(2024, 1, 15);
        CellValue value = new DateValue(date);
        LocalDate result = switch (value) {
            case DateValue(var d) -> d;
            default -> null;
        };
        assertThat(result).isEqualTo(date);
    }

    @Test
    void patternMatchErrorValue() {
        CellValue value = new ErrorValue(ErrorType.DIV_BY_ZERO);
        ErrorType type = switch (value) {
            case ErrorValue(var t) -> t;
            default -> null;
        };
        assertThat(type).isEqualTo(ErrorType.DIV_BY_ZERO);
    }

    @Test
    void blankValueEquality() {
        assertThat(new BlankValue()).isEqualTo(new BlankValue());
    }

    @Test
    void allPermitsExhaustive() {
        CellValue[] values = {
            new TextValue("x"),
            new NumberValue(1.0),
            new BooleanValue(true),
            new ErrorValue(ErrorType.VALUE),
            new BlankValue(),
            new DateValue(LocalDate.now()),
            new DateTimeValue(LocalDateTime.now())
        };
        for (CellValue v : values) {
            String label = switch (v) {
                case TextValue ignored -> "text";
                case NumberValue ignored -> "number";
                case BooleanValue ignored -> "boolean";
                case ErrorValue ignored -> "error";
                case BlankValue ignored -> "blank";
                case DateValue ignored -> "date";
                case DateTimeValue ignored -> "datetime";
            };
            assertThat(label).isNotNull();
        }
    }
}
