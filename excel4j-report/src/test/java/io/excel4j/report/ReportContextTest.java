package io.excel4j.report;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ReportContextTest {

    @Test
    void createAndGetValue() {
        var ctx = ReportContext.create().set("title", "Sales Report");
        assertThat(ctx.get("title")).isEqualTo("Sales Report");
    }

    @Test
    void setReturnsNewInstance() {
        var ctx1 = ReportContext.create().set("a", 1);
        var ctx2 = ctx1.set("b", 2);
        assertThat(ctx1.get("b")).isNull();
        assertThat(ctx2.get("a")).isEqualTo(1);
        assertThat(ctx2.get("b")).isEqualTo(2);
    }

    @Test
    void withItemAndGetItem() {
        var ctx = ReportContext.create().set("title", "Report");
        var itemCtx = ctx.withItem(java.util.Map.of("product", "Widget"));
        assertThat(itemCtx.getItem()).isNotNull();
        assertThat(itemCtx.get("title")).isEqualTo("Report");
    }

    @Test
    void getMissingKeyReturnsNull() {
        var ctx = ReportContext.create();
        assertThat(ctx.get("missing")).isNull();
    }
}
