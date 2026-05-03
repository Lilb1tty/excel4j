# excel4j

[![Maven Central](https://img.shields.io/maven-central/v/io.github.lilb1tty/excel4j-core.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.lilb1tty/excel4j-core)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-21%2B-orange.svg)](https://openjdk.org/projects/jdk/21/)

Pure Java library for reading, writing, and templating Excel `.xlsx` files. Requires Java 21 or later.

Inspired by [FlexCel](https://www.tmssoftware.com/site/flexcelnet.asp) (.NET) — built from scratch with zero external runtime dependencies. Not a wrapper over Apache POI.

```java
// Create (named first sheet, no extra Sheet1)
Workbook wb = Excel.create("Sales");
Worksheet sheet = wb.sheet("Sales");
sheet.cell("A1").setValue(new TextValue("Q1 Revenue"));
sheet.cell("B1").setValue(new NumberValue(125000.50));
Excel.write(wb, Path.of("report.xlsx"));

// Read
Workbook read = Excel.read(Path.of("report.xlsx"));
CellValue value = read.sheet("Sales").cell("B1").getValue();

// Template report
Workbook template = Excel.read(Path.of("invoice-template.xlsx"));
ReportContext ctx = ReportContext.create()
    .set("customer", "Acme Corp")
    .set("items", List.of(item1, item2));
Workbook output = new ReportEngine().render(template, ctx);
Excel.write(output, Path.of("invoice-output.xlsx"));
```

## Features

- **Zero runtime dependencies** — pure Java, no third-party runtime deps
- **Read & write `.xlsx`** — StAX streaming, low memory footprint
- **Streaming read API** — row-by-row `Stream<Row>` for large files without full workbook load
- **Formula evaluation** — 98 built-in functions with dependency graph and circular reference handling
- **Template engine** — FlexCel-style `<#value>` and `<#band>` tags for report generation
- **Pivot tables** — write pivot tables with row fields and data aggregations
- **PNG/JPEG rendering** — render worksheets to images via AWT (headless-safe)
- **PDF export** — render workbooks to PDF, one page per sheet
- **Range selection** — apply styles and set values across a cell range in one call
- **Type-safe cell values** — sealed `CellValue` interface with pattern-matching support
- **Full JPMS** — `module-info.java` per module, strict compile-time boundaries
- **Modern Java** — records, sealed interfaces, pattern matching switch

## Requirements

- Java 21 or later (Java 21 is the minimum required version)
- Maven 3.9+

## Installation

**Maven:**

```xml
<dependency>
  <groupId>io.github.lilb1tty</groupId>
  <artifactId>excel4j-core</artifactId>
  <version>1.1.0</version>
</dependency>
```

**Gradle (Kotlin DSL):**

```kotlin
implementation("io.github.lilb1tty:excel4j-core:1.1.0")
```

**Gradle (Groovy):**

```groovy
implementation 'io.github.lilb1tty:excel4j-core:1.1.0'
```

Or use the BOM to manage all module versions at once:

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>io.github.lilb1tty</groupId>
      <artifactId>excel4j-bom</artifactId>
      <version>1.1.0</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

Modules:
- `excel4j-core` — XLSX I/O + workbook/cell model
- `excel4j-formula` — Formula parser, AST, evaluator
- `excel4j-report` — Template-based report generation
- `excel4j-render` — PNG/JPEG/PDF rendering

## Quick Start

### Create a workbook

```java
import io.github.lilb1tty.excel4j.core.Excel;
import io.github.lilb1tty.excel4j.core.model.*;
import io.github.lilb1tty.excel4j.core.model.style.*;
import java.nio.file.Path;
import java.time.LocalDate;

Workbook wb = Excel.create("Orders");
Worksheet sheet = wb.sheet("Orders");

// Values
sheet.cell("A1").setValue(new TextValue("Product"));
sheet.cell("B1").setValue(new TextValue("Price"));
sheet.cell("C1").setValue(new TextValue("Date"));

sheet.cell("A2").setValue(new TextValue("Widget"));
sheet.cell("B2").setValue(new NumberValue(29.99));
sheet.cell("C2").setValue(new DateValue(LocalDate.now()));

// Formula
sheet.cell("B5").setFormula("=SUM(B2:B4)");

// Style
CellStyle headerStyle = CellStyle.DEFAULT
    .withFont(Font.DEFAULT.bold())
    .withFill(Fill.solid("#4472C4"));
sheet.cell("A1").setStyle(headerStyle);
sheet.cell("B1").setStyle(headerStyle);
sheet.cell("C1").setStyle(headerStyle);

Excel.write(wb, Path.of("orders.xlsx"));
```

### Range selection

Apply a style or set values across multiple cells in one call:

```java
CellStyle headerStyle = CellStyle.DEFAULT
    .withFont(Font.DEFAULT.bold())
    .withFill(Fill.solid("#4472C4"));

// Apply style to A1:C1
sheet.range("A1", "C1").setStyle(headerStyle);

// Set values row by row (each inner list is one row)
sheet.range("A1", "C2").setValues(List.of(
    List.of(new TextValue("Product"), new TextValue("Price"), new TextValue("Date")),
    List.of(new TextValue("Widget"), new NumberValue(29.99), new DateValue(LocalDate.now()))
));

// Chain style + values
sheet.range("A1", "C1")
    .setStyle(headerStyle)
    .setValues(List.of(
        List.of(new TextValue("Product"), new TextValue("Price"), new TextValue("Date"))
    ));
```

`setValues` fills the range row by row. Values that exceed the range bounds are ignored.

### Read a workbook

```java
Workbook wb = Excel.read(Path.of("orders.xlsx"));
Worksheet sheet = wb.sheet("Orders");

CellValue value = sheet.cell("B2").getValue();
String text = switch (value) {
    case TextValue tv    -> tv.value();
    case NumberValue nv  -> String.valueOf(nv.value());
    case DateValue dv    -> dv.value().toString();
    case BlankValue bv   -> "";
    default              -> value.toString();
};
```

### Stream rows (large files)

For large files, stream rows one at a time without loading the full workbook:

```java
import io.github.lilb1tty.excel4j.core.io.SheetStreamReader;

try (SheetStreamReader reader = Excel.streamReader(Path.of("large.xlsx"), 1)) {
    reader.stream()
        .skip(1)                          // skip header row
        .filter(row -> row.rowNum() < 1000)
        .forEach(row -> {
            CellValue name  = row.cell(1); // column A (1-based)
            CellValue score = row.cell(2); // column B
            // process...
        });
}
```

`SheetStreamReader` is `AutoCloseable` — always use `try-with-resources`. Shared strings are loaded once upfront; worksheet XML is parsed row-by-row, one `Row` in memory at a time.

### Evaluate formulas

```java
import io.github.lilb1tty.excel4j.formula.FormulaEvaluator;

Workbook wb = Excel.read(Path.of("sheet-with-formulas.xlsx"));
FormulaEvaluator evaluator = new FormulaEvaluator(wb);
wb.recalculate(evaluator::evaluate);

// Now cells with formulas have computed cached values
CellValue result = wb.sheet(1).cell("B5").getCachedValue();
```

### Template reports

Design an `.xlsx` template with tags:

| Tag | Meaning |
|-----|---------|
| `<#value fieldName>` | Substitute with value from context |
| `<#band items>` | Start repeating band |
| `</band>` | End repeating band |

```java
import io.github.lilb1tty.excel4j.report.*;

Workbook template = Excel.read(Path.of("invoice-template.xlsx"));

ReportContext ctx = ReportContext.create()
    .set("company", "Acme Corp")
    .set("date", LocalDate.now())
    .set("lineItems", List.of(
        Map.of("product", "Widget", "qty", 10, "price", 29.99),
        Map.of("product", "Gadget", "qty", 5, "price", 49.99)
    ));

ReportEngine engine = new ReportEngine();
Workbook output = engine.render(template, ctx);
Excel.write(output, Path.of("invoice-2024-001.xlsx"));
```

The engine expands bands row-by-row, substitutes values, and preserves styles and formulas.

### Pivot tables

```java
Workbook wb = Excel.create();
Worksheet sheet = wb.addSheet("Sales");

// Source data
sheet.cell("A1").setValue(new TextValue("Product"));
sheet.cell("B1").setValue(new TextValue("Region"));
sheet.cell("C1").setValue(new TextValue("Amount"));
sheet.cell("A2").setValue(new TextValue("Widget"));
sheet.cell("B2").setValue(new TextValue("North"));
sheet.cell("C2").setValue(new NumberValue(1250));

PivotTable pt = PivotTable.builder()
    .name("SalesSummary")
    .sourceRange(CellRange.of("A1", "C10"))
    .rowField("Product")
    .rowField("Region")
    .dataField("Amount", "sum")
    .location("E1")
    .build();
sheet.addPivotTable(pt);

Excel.write(wb, Path.of("report.xlsx"));
```

Excel regenerates pivot cache data on open. Only pivot layout definition is written.

### Render to PNG/JPEG

```java
import io.github.lilb1tty.excel4j.render.SheetRenderer;
import io.github.lilb1tty.excel4j.render.RenderOptions;

Workbook wb = Excel.read(Path.of("report.xlsx"));
Worksheet sheet = wb.sheet(1);

// Default options (96px wide columns, 22px tall rows, 11pt font)
SheetRenderer.toPng(sheet, Path.of("sheet.png"));
SheetRenderer.toJpeg(sheet, Path.of("sheet.jpg"));

// Custom options
RenderOptions opts = new RenderOptions(120, 28, 13, 4);
SheetRenderer.toPng(sheet, Path.of("sheet-large.png"), opts);

// Or get a BufferedImage directly
BufferedImage img = SheetRenderer.toImage(sheet, RenderOptions.defaults());
```

### Render to PDF

```java
import io.github.lilb1tty.excel4j.render.PdfRenderer;

Workbook wb = Excel.read(Path.of("report.xlsx"));

// One page per worksheet, A4 portrait
PdfRenderer.toPdf(wb, Path.of("report.pdf"));

// Single sheet
PdfRenderer.toPdf(wb.sheet(1), Path.of("page1.pdf"));
```

Rendering uses `java.awt` in headless mode — no display required. PDF output embeds rendered images into a minimal PDF 1.4 document. Requires the `excel4j-render` module.

## Architecture

```
excel4j/
├── excel4j-bom/          Bill of materials
├── excel4j-core/         XLSX I/O + workbook/cell model
├── excel4j-formula/      Formula parser, AST, evaluator
├── excel4j-report/       Template engine (FlexCel-style)
└── excel4j-render/       PNG/JPEG/PDF rendering (AWT + raw PDF)
```

Dependency direction: `report` → `formula` → `core`

JPMS enforces boundaries at compile time — no reflection hacks needed.

## Cell Value Model

```java
public sealed interface CellValue
    permits TextValue, NumberValue, BooleanValue,
            ErrorValue, BlankValue, DateValue, DateTimeValue {}
```

All first-class types. No `instanceof` chains needed — use pattern matching switch.

## Formula Functions (v1)

98 built-in functions covering math, logic, text, date/time, lookup/statistical, and arrays:

**Math:** `SUM`, `PRODUCT`, `MIN`, `MAX`, `ABS`, `ROUND`, `INT`, `MOD`, `POWER`, `SQRT`, `COUNT`, `AVERAGE`, `CEILING`, `FLOOR`, `ROUNDUP`, `ROUNDDOWN`, `TRUNC`, `SIGN`, `LOG`, `LOG10`, `LN`, `EXP`, `PI`, `RAND`, `RANDBETWEEN`, `FACT`, `SUMPRODUCT`, `SUMSQ`

**Logic:** `IF`, `AND`, `OR`, `NOT`, `IFERROR`, `ISBLANK`, `ISNUMBER`, `ISTEXT`, `ISLOGICAL`, `ISERROR`, `XOR`, `IFS`, `SWITCH`, `IFNA`

**Text:** `LEFT`, `RIGHT`, `MID`, `LEN`, `TRIM`, `CONCATENATE`, `UPPER`, `LOWER`, `REPT`, `FIND`, `SUBSTITUTE`, `EXACT`, `SEARCH`, `REPLACE`, `CHAR`, `CODE`, `VALUE`, `T`

**Date/Time:** `DATE`, `TODAY`, `NOW`, `YEAR`, `MONTH`, `DAY`, `HOUR`, `MINUTE`, `SECOND`, `WEEKDAY`, `EDATE`, `EOMONTH`, `DAYS`, `TIME`, `DATEDIF`, `ISOWEEKNUM`

**Lookup/Stat:** `VLOOKUP`, `INDEX`, `MATCH`, `COUNTA`, `COUNTIF`, `SUMIF`, `AVERAGEIF`, `HLOOKUP`, `CHOOSE`, `LARGE`, `SMALL`, `RANK`, `MEDIAN`, `MODE`, `STDEV`, `VAR`, `COUNTIFS`, `SUMIFS`, `AVERAGEIFS`

**Array:** `SEQUENCE`, `UNIQUE`, `SORT`

Custom functions are extensible via `FunctionRegistry`.

**Array literals** — `{1,2;3,4}` syntax for inline arrays. Row separator `,`, column separator `;`. Arrays evaluate to `RangeValue` and work with any function that accepts ranges.

See the full [Function Reference](docs/functions.md) for syntax, parameters, and examples.

## Roadmap

### v1 (Current)
- [x] XLSX read/write with StAX streaming
- [x] Type-safe cell model with styles
- [x] Formula parser + evaluator (98 functions)
- [x] Array formulas — `{1,2;3,4}` literals + SEQUENCE, UNIQUE, SORT
- [x] Charts — bar, line, pie (write path)
- [x] Template-based report generation
- [x] Full JPMS module boundaries
- [x] Streaming read API — `Stream<Row>` for large files
- [x] Expanded function library (50 → 98)
- [x] Array formulas / dynamic arrays
- [x] Pivot tables (write path)
- [x] PNG/JPEG rendering — `excel4j-render` (`SheetRenderer`)
- [x] PDF export — `excel4j-render` (`PdfRenderer`)

## Documentation

- [Function Reference](docs/functions.md) — all 98 formula functions with syntax and examples

## Build

```bash
mvn clean test
```

Requires Java 21 or later. No `--enable-preview` features.

## Changelog

### v1.1.0 — 2026-05-03

**excel4j-core**
- `Excel.create(String sheetName)` — creates a workbook with a named first sheet, no extra default Sheet1
- `Worksheet.range(String from, String to)` — returns a `RangeSelection` for bulk style and value operations
- `RangeSelection.setStyle(CellStyle)` — applies a style to every cell in the range
- `RangeSelection.setValues(List<List<CellValue>>)` — sets values row-by-row across the range
- Fixed OOXML writer: cells now correctly wrapped in `<row r="N">` elements (was causing empty sheets in Excel)
- Fixed OOXML writer: solid fill colors now written to `styles.xml` (was silently dropped)
- Fixed OOXML writer: `fontId` and `fillId` in `cellXfs` now reference correct deduplicated indexes
- Fixed OOXML writer: `<f>` (formula) element now written before `<v>` (value) per OOXML schema
- Fixed OOXML writer: `Content_Types.xml` now includes required `Default` entries for `.rels` and `.xml`
- Fixed OOXML writer: `applyFont` and `applyFill` attributes now set correctly in `cellXfs`

### v1.0.0 — 2026-05-03

**Release**
- Published to Maven Central (`io.github.lilb1tty`)
- All five artifacts available: `excel4j-core`, `excel4j-formula`, `excel4j-report`, `excel4j-render`, `excel4j-bom`

### v1.6.0 — 2026-05-02

**excel4j-render** (new module)
- `SheetRenderer` — renders worksheets to `BufferedImage`, PNG, or JPEG using `java.awt` (headless-safe)
- `PdfRenderer` — renders workbooks to PDF (one A4 page per sheet) using raw PDF 1.4 writing with embedded images
- `RenderOptions` record — configure cell dimensions, font size, and padding
- Zero external dependencies — `java.desktop` module only

### v1.5.0 — 2026-05-02

**excel4j-core**
- Pivot table support (write path): row fields + data fields with aggregation
- `PivotTable` builder API with `sourceRange`, `rowField`, `dataField`, and `location`
- `Worksheet.addPivotTable()` attaches pivot tables to worksheets
- Full OOXML pivotCache/pivotTable/rels plumbing written to XLSX output

### v1.4.0 — 2026-05-02

**excel4j-core**
- Chart support (write path): bar, line, pie charts
- `Chart` builder API with title, type, categories, series, and position
- `Worksheet.addChart()` attaches charts to worksheets
- Full OOXML chart/drawing/rels plumbing written to XLSX output

### v1.3.0 — 2026-05-02

**excel4j-formula**
- Array formula literals: `{1,2;3,4}` syntax with `,` (column) and `;` (row) separators
- New array functions: `SEQUENCE`, `UNIQUE`, `SORT`
- Total functions now 98 (up from 95)

### v1.2.0 — 2026-05-02

**excel4j-formula**
- Added 45 new functions (50 → 95 total):
  - Math: `CEILING`, `FLOOR`, `ROUNDUP`, `ROUNDDOWN`, `TRUNC`, `SIGN`, `LOG`, `LOG10`, `LN`, `EXP`, `PI`, `RAND`, `RANDBETWEEN`, `FACT`, `SUMPRODUCT`, `SUMSQ`
  - Logic: `XOR`, `IFS`, `SWITCH`, `IFNA`
  - Text: `EXACT`, `SEARCH`, `REPLACE`, `CHAR`, `CODE`, `VALUE`, `T`
  - Date/Time: `EDATE`, `EOMONTH`, `DAYS`, `TIME`, `DATEDIF`, `ISOWEEKNUM`
  - Lookup/Stat: `HLOOKUP`, `CHOOSE`, `LARGE`, `SMALL`, `RANK`, `MEDIAN`, `MODE`, `STDEV`, `VAR`, `COUNTIFS`, `SUMIFS`, `AVERAGEIFS`

### v1.1.0 — 2026-05-02

**excel4j-core**
- Added `Row` record: row number + column-indexed `CellValue` map with `cell(int col)` helper
- Added `SheetStreamReader`: `AutoCloseable` row-by-row XLSX streaming via StAX — works with both standard OOXML `<row>` elements and excel4j's own write format
- Added `Excel.streamReader(Path, int)` and `Excel.streamReader(String, int)` entry points

### v1.0.0 — 2026-05-01

Initial release.

**excel4j-core**
- XLSX read/write via StAX streaming (low memory, handles large files)
- Type-safe cell value model: `TextValue`, `NumberValue`, `BooleanValue`, `DateValue`, `DateTimeValue`, `ErrorValue`, `BlankValue`
- Immutable `CellStyle` record with `Font`, `Fill`, `Border`, `NumberFormat`
- Predefined number format constants + `NumberFormat.custom()`
- A1 notation and row/col (1-based) cell addressing
- `Excel.create()`, `Excel.read()`, `Excel.write()` static entry points

**excel4j-formula**
- Formula tokenizer, recursive-descent parser, AST evaluator
- 50 built-in functions: math, logic, text, date/time, lookup/statistical
- Circular reference detection — affected cells get `#CIRCULAR_REF`, rest continue
- Extensible `FunctionRegistry` for custom functions
- Full Excel error type propagation

**excel4j-report**
- FlexCel-style template engine with `<#value name>` and `<#band name>` / `</band>` tags
- Band expansion: repeats rows for each item in a collection
- Value substitution with dot-notation (`customer.name`), JavaBean getters, `Map` keys
- Type-aware substitution: pure value tags get typed `CellValue`; mixed text gets string replacement
- Style and formula preservation across expanded rows

## License

Apache License 2.0 — see [LICENSE](LICENSE) for details.
