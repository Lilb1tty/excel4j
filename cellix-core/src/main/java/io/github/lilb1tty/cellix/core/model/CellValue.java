package io.github.lilb1tty.cellix.core.model;

public sealed interface CellValue
        permits TextValue, NumberValue, BooleanValue,
                ErrorValue, BlankValue,
                DateValue, DateTimeValue, RangeValue {}
