package io.excel4j.report;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class TagParserTest {

    @Test
    void parseValueTag() {
        var tags = TagParser.parse("<#value title>");
        assertThat(tags).hasSize(1);
        assertThat(tags.get(0)).isInstanceOf(Tag.ValueTag.class);
        assertThat(((Tag.ValueTag) tags.get(0)).name()).isEqualTo("title");
    }

    @Test
    void parseBandStartTag() {
        var tags = TagParser.parse("<#band items>");
        assertThat(tags).hasSize(1);
        assertThat(tags.get(0)).isInstanceOf(Tag.BandStartTag.class);
        assertThat(((Tag.BandStartTag) tags.get(0)).name()).isEqualTo("items");
    }

    @Test
    void parseBandEndTag() {
        var tags = TagParser.parse("</band>");
        assertThat(tags).hasSize(1);
        assertThat(tags.get(0)).isInstanceOf(Tag.BandEndTag.class);
    }

    @Test
    void parseMultipleTags() {
        var tags = TagParser.parse("Name: <#value first> <#value last>");
        assertThat(tags).hasSize(2);
        assertThat(((Tag.ValueTag) tags.get(0)).name()).isEqualTo("first");
        assertThat(((Tag.ValueTag) tags.get(1)).name()).isEqualTo("last");
    }

    @Test
    void parseNoTags() {
        var tags = TagParser.parse("Hello World");
        assertThat(tags).isEmpty();
    }

    @Test
    void parseValueWithDotNotation() {
        var tags = TagParser.parse("<#value item.product>");
        assertThat(tags).hasSize(1);
        assertThat(((Tag.ValueTag) tags.get(0)).name()).isEqualTo("item.product");
    }
}
