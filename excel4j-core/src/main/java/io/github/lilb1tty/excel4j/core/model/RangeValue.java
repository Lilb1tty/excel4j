package io.github.lilb1tty.excel4j.core.model;

import java.util.List;

public record RangeValue(CellRange range, List<CellValue> values) implements CellValue {}
