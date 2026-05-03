package io.github.lilb1tty.cellix.report;

import io.github.lilb1tty.cellix.core.model.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class ValueResolverTest {

    @Test
    void resolveRootValue() {
        var ctx = ReportContext.create().set("title", "Hello");
        assertThat(ValueResolver.resolve(ctx, "title"))
            .isEqualTo(new TextValue("Hello"));
    }

    @Test
    void resolveScopedValue() {
        var ctx = ReportContext.create()
            .withItem(Map.of("product", "Widget", "qty", 10));
        assertThat(ValueResolver.resolve(ctx, "product"))
            .isEqualTo(new TextValue("Widget"));
        assertThat(ValueResolver.resolve(ctx, "qty"))
            .isEqualTo(new NumberValue(10.0));
    }

    @Test
    void resolveDotNotationFromRoot() {
        var ctx = ReportContext.create()
            .set("item", Map.of("name", "Gadget"));
        assertThat(ValueResolver.resolve(ctx, "item.name"))
            .isEqualTo(new TextValue("Gadget"));
    }

    @Test
    void resolveNullReturnsBlank() {
        var ctx = ReportContext.create().set("missing", null);
        assertThat(ValueResolver.resolve(ctx, "missing"))
            .isInstanceOf(BlankValue.class);
    }

    @Test
    void resolveNumberValue() {
        var ctx = ReportContext.create().set("price", 29.99);
        assertThat(ValueResolver.resolve(ctx, "price"))
            .isEqualTo(new NumberValue(29.99));
    }

    @Test
    void resolveBooleanValue() {
        var ctx = ReportContext.create().set("active", true);
        assertThat(ValueResolver.resolve(ctx, "active"))
            .isEqualTo(new BooleanValue(true));
    }

    @Test
    void resolveLocalDateValue() {
        var date = LocalDate.of(2024, 1, 15);
        var ctx = ReportContext.create().set("date", date);
        assertThat(ValueResolver.resolve(ctx, "date"))
            .isEqualTo(new DateValue(date));
    }

    @Test
    void resolveLocalDateTimeValue() {
        var dt = LocalDateTime.of(2024, 1, 15, 10, 30);
        var ctx = ReportContext.create().set("dt", dt);
        assertThat(ValueResolver.resolve(ctx, "dt"))
            .isEqualTo(new DateTimeValue(dt));
    }

    @Test
    void resolveUnknownReturnsBlank() {
        var ctx = ReportContext.create();
        assertThat(ValueResolver.resolve(ctx, "unknown"))
            .isInstanceOf(BlankValue.class);
    }
}
