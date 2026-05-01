package io.excel4j.core.model;

public sealed interface CellValue
        permits TextValue, NumberValue, BooleanValue,
                ErrorValue, BlankValue, DateValue, DateTimeValue {}
