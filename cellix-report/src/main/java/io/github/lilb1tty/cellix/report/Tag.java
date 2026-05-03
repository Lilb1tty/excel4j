package io.github.lilb1tty.cellix.report;

public sealed interface Tag {
    record ValueTag(String name) implements Tag {}
    record BandStartTag(String name) implements Tag {}
    record BandEndTag() implements Tag {}
}
