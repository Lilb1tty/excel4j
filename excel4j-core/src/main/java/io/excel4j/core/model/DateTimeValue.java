package io.excel4j.core.model;

import java.time.LocalDateTime;

public record DateTimeValue(LocalDateTime value) implements CellValue {}
