package io.github.lilb1tty.excel4j.core.model;

import java.time.LocalDateTime;

public record DateTimeValue(LocalDateTime value) implements CellValue {}
