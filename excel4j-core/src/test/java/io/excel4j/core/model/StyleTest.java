package io.excel4j.core.model;

import io.excel4j.core.model.style.*;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class StyleTest {

    @Test
    void fontDefaultValues() {
        assertThat(Font.DEFAULT.name()).isEqualTo("Calibri");
        assertThat(Font.DEFAULT.size()).isEqualTo(11.0);
        assertThat(Font.DEFAULT.isBold()).isFalse();
    }

    @Test
    void fontBoldReturnsCopy() {
        Font bold = Font.DEFAULT.bold();
        assertThat(bold.isBold()).isTrue();
        assertThat(Font.DEFAULT.isBold()).isFalse(); // original unchanged
    }

    @Test
    void fontItalicReturnsCopy() {
        Font italic = Font.DEFAULT.italic();
        assertThat(italic.isItalic()).isTrue();
        assertThat(Font.DEFAULT.isItalic()).isFalse();
    }

    @Test
    void fillNoneIsDefault() {
        assertThat(Fill.NONE.pattern()).isEqualTo(Fill.FillPattern.NONE);
    }

    @Test
    void fillSolid() {
        Fill fill = Fill.solid("FF0000");
        assertThat(fill.pattern()).isEqualTo(Fill.FillPattern.SOLID);
        assertThat(fill.foregroundColor()).isEqualTo("FF0000");
    }

    @Test
    void borderNone() {
        assertThat(Border.NONE.top()).isEqualTo(Border.BorderStyle.NONE);
        assertThat(Border.NONE.bottom()).isEqualTo(Border.BorderStyle.NONE);
    }

    @Test
    void borderThin() {
        Border thin = Border.thin();
        assertThat(thin.top()).isEqualTo(Border.BorderStyle.THIN);
        assertThat(thin.left()).isEqualTo(Border.BorderStyle.THIN);
    }

    @Test
    void numberFormatPredefined() {
        assertThat(NumberFormat.GENERAL.formatCode()).isEqualTo("General");
        assertThat(NumberFormat.DECIMAL_2.formatCode()).isEqualTo("#,##0.00");
        assertThat(NumberFormat.DATE_SHORT.formatCode()).isEqualTo("yyyy-mm-dd");
    }

    @Test
    void numberFormatCustom() {
        NumberFormat fmt = NumberFormat.custom("0.000");
        assertThat(fmt.formatCode()).isEqualTo("0.000");
    }

    @Test
    void numberFormatIsDate() {
        assertThat(NumberFormat.DATE_SHORT.isDateFormat()).isTrue();
        assertThat(NumberFormat.DECIMAL_2.isDateFormat()).isFalse();
    }

    @Test
    void cellStyleDefault() {
        assertThat(CellStyle.DEFAULT.font()).isEqualTo(Font.DEFAULT);
        assertThat(CellStyle.DEFAULT.fill()).isEqualTo(Fill.NONE);
        assertThat(CellStyle.DEFAULT.border()).isEqualTo(Border.NONE);
        assertThat(CellStyle.DEFAULT.numberFormat()).isEqualTo(NumberFormat.GENERAL);
    }

    @Test
    void cellStyleWithFontReturnsCopy() {
        Font bold = Font.DEFAULT.bold();
        CellStyle boldStyle = CellStyle.DEFAULT.withFont(bold);
        assertThat(boldStyle.font().isBold()).isTrue();
        assertThat(CellStyle.DEFAULT.font().isBold()).isFalse(); // unchanged
    }

    @Test
    void cellStyleEqualityByValue() {
        CellStyle a = CellStyle.DEFAULT.withFont(Font.DEFAULT.bold());
        CellStyle b = CellStyle.DEFAULT.withFont(Font.DEFAULT.bold());
        assertThat(a).isEqualTo(b); // records use structural equality
        assertThat(a.hashCode()).isEqualTo(b.hashCode()); // required for StyleTable dedup
    }
}
