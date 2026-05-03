package io.github.lilb1tty.excel4j.core.model;

import java.time.LocalDate;

public record DateValue(LocalDate value) implements CellValue {}
