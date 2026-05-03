package io.github.lilb1tty.excel4j.core.model;

public sealed interface CellValue
        permits TextValue, NumberValue, BooleanValue,
                ErrorValue, BlankValue,
                DateValue, DateTimeValue, RangeValue {}
