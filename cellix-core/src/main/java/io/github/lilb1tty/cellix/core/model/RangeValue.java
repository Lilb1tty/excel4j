package io.github.lilb1tty.cellix.core.model;

import java.util.List;

public record RangeValue(CellRange range, List<CellValue> values) implements CellValue {}
