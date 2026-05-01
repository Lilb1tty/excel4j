# excel4j

Pure JDK 21 library for reading, writing, and templating Excel `.xlsx` files.

Inspired by [FlexCel](https://www.tmssoftware.com/site/flexcelnet.asp) (.NET) — built from scratch with zero external runtime dependencies. Not a wrapper over Apache POI.

```java
// Create
Workbook wb = Excel.create();
wb.sheet("Sales").cell("A1").setValue(new TextValue("Q1 Revenue"));
wb.sheet("Sales").cell("B1").setValue(new NumberValue(125000.50));
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

- **Zero runtime dependencies** — JDK 21 only
- **Read & write `.xlsx`** — StAX streaming, low memory footprint
- **Formula evaluation** — 50+ built-in functions with dependency graph and circular reference handling
- **Template engine** — FlexCel-style `<#value>` and `<#band>` tags for report generation
- **Type-safe cell values** — sealed `CellValue` interface with pattern-matching support
- **Full JPMS** — `module-info.java` per module, strict compile-time boundaries
- **Modern Java** — records, sealed interfaces, pattern matching switch

## Requirements

- Java 21 (LTS)
- Maven 3.9+

## Installation

```xml
<dependency>
  <groupId>io.excel4j</groupId>
  <artifactId>excel4j-core</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
```

Or use the BOM to manage all module versions:

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>io.excel4j</groupId>
      <artifactId>excel4j-bom</artifactId>
      <version>1.0.0-SNAPSHOT</version>
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

## Quick Start

### Create a workbook

```java
import io.excel4j.core.Excel;
import io.excel4j.core.model.*;
import io.excel4j.core.model.style.*;
import java.nio.file.Path;
import java.time.LocalDate;

Workbook wb = Excel.create();
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
    .withFont(Font.DEFAULT.withBold(true))
    .withFill(Fill.solid("#4472C4"));
sheet.cell("A1").setStyle(headerStyle);
sheet.cell("B1").setStyle(headerStyle);
sheet.cell("C1").setStyle(headerStyle);

Excel.write(wb, Path.of("orders.xlsx"));
```

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

### Evaluate formulas

```java
import io.excel4j.formula.FormulaEvaluator;

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
import io.excel4j.report.*;

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

## Architecture

```
excel4j/
├── excel4j-bom/          Bill of materials
├── excel4j-core/         XLSX I/O + workbook/cell model
├── excel4j-formula/      Formula parser, AST, evaluator
├── excel4j-report/       Template engine (FlexCel-style)
└── excel4j-render/       PDF/image rendering (v2 stub)
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

50 built-in functions covering math, logic, text, date/time, and lookup/statistical:

**Math:** `SUM`, `PRODUCT`, `MIN`, `MAX`, `ABS`, `ROUND`, `INT`, `MOD`, `POWER`, `SQRT`, `COUNT`, `AVERAGE`

**Logic:** `IF`, `AND`, `OR`, `NOT`, `IFERROR`, `ISBLANK`, `ISNUMBER`, `ISTEXT`, `ISLOGICAL`, `ISERROR`

**Text:** `LEFT`, `RIGHT`, `MID`, `LEN`, `TRIM`, `CONCATENATE`, `UPPER`, `LOWER`, `REPT`, `FIND`, `SUBSTITUTE`

**Date/Time:** `DATE`, `TODAY`, `NOW`, `YEAR`, `MONTH`, `DAY`, `HOUR`, `MINUTE`, `SECOND`, `WEEKDAY`

**Lookup/Stat:** `VLOOKUP`, `INDEX`, `MATCH`, `COUNTA`, `COUNTIF`, `SUMIF`, `AVERAGEIF`

Custom functions are extensible via `FunctionRegistry`.

See the full [Function Reference](docs/functions.md) for syntax, parameters, and examples.

## Roadmap

### v1 (Current)
- [x] XLSX read/write with StAX streaming
- [x] Type-safe cell model with styles
- [x] Formula parser + evaluator (50+ functions)
- [x] Template-based report generation
- [x] Full JPMS module boundaries

### v2
- [ ] PDF/image rendering (`excel4j-render`)
- [ ] Streaming read API for large files
- [ ] Array formulas / dynamic arrays
- [ ] Charts
- [ ] Pivot tables
- [ ] Expanded function library

## Documentation

- [Function Reference](docs/functions.md) — all 50 formula functions with syntax and examples

## Build

```bash
mvn clean test
```

Requires Java 21. No `--enable-preview` features.

## Changelog

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
