package io.excel4j.core.model;

import java.time.LocalDate;

public record DateValue(LocalDate value) implements CellValue {}
