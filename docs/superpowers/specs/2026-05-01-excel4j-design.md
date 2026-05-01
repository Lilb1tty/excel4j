# excel4j Design Spec
**Date:** 2026-05-01
**Status:** Approved

## Summary

Java reimplementation of FlexCel (.NET) for JDK 21. FlexCel-inspired API ergonomics — template engine, formula evaluator, clean read/write model — built from scratch with zero external runtime dependencies. Open source, Apache 2.0, published on GitHub.

Not a port of .NET source (proprietary). Not a wrapper over Apache POI. Pure JDK implementation.

---

## Goals

- v1: Read/write XLSX + formula evaluation + template-based report generation
- v2: PDF/image rendering, streaming read API, array formula support
- Zero runtime dependencies (JDK 21 only)
- FlexCel-quality developer experience
- Apache 2.0, Maven Central

---

## Module Architecture

Maven multi-module + full JPMS (`module-info.java` per module). Strict one-directional dependency graph, no cycles.

```
excel4j/
├── excel4j-bom/          ← bill of materials
├── excel4j-core/         ← OOXML I/O + workbook/cell model
├── excel4j-formula/      ← formula parser, AST, evaluator, function registry
├── excel4j-report/       ← template engine (FlexCelReport-style)
└── excel4j-render/       ← PDF/image rendering (v2 stub only)
```

Dependency direction:
```
report  →  formula  →  core
render  →  core
```

JPMS enforces layer boundaries at compile time — `excel4j-report` cannot reach into unexported `excel4j-core` internals.

```java
// excel4j-core module-info.java
module io.excel4j.core {
    exports io.excel4j.core.model;
    exports io.excel4j.core.io;
}
```

Users depend on the highest module they need:
- XLSX I/O only → `excel4j-core`
- Formula evaluation → `excel4j-formula`
- Template reports → `excel4j-report`

---

## Top-Level API

Single entry point via static factory:

```java
// create new workbook
Workbook wb = Excel.create();

// read existing file
Workbook wb = Excel.read(path);

// template report
Excel.report(templatePath)
     .bind("Orders", orders)
     .bind("Summary", summary)
     .generate(outputPath);
```

---

## excel4j-core

### Cell Value Model

```java
sealed interface CellValue
    permits TextValue, NumberValue, BooleanValue,
            ErrorValue, BlankValue,
            DateValue, DateTimeValue {}

record TextValue(String value)          implements CellValue {}
record NumberValue(double value)        implements CellValue {}
record BooleanValue(boolean value)      implements CellValue {}
record ErrorValue(ErrorType type)       implements CellValue {}
record BlankValue()                     implements CellValue {}
record DateValue(LocalDate value)       implements CellValue {}
record DateTimeValue(LocalDateTime value) implements CellValue {}
```

Reader detects date number format → returns `DateValue` / `DateTimeValue` automatically.
Writer accepts `DateValue` → converts to double + sets number format automatically.
Handles Excel's 1900 leap year bug on conversion.

Pattern matching via `switch` — no `instanceof` chains.

### Key Records

```java
record CellRef(int row, int col) {}           // 1-based, public API
record CellRange(CellRef first, CellRef last) {}
record WorksheetName(String value) {}
```

All public-facing row/col coordinates are **1-based** (row 1 col 1 = A1).

### Workbook Model

Mutable. Not thread-safe — caller's responsibility.

```
Workbook  (mutable)
└── Worksheet[]  (mutable)
    └── Cell  (ref, value, formula?, style)
```

**Workbook API:**
```java
Worksheet sheet = workbook.sheet("Orders");    // by name
Worksheet sheet = workbook.sheet(1);           // by index (1-based)
List<Worksheet> sheets = workbook.sheets();    // iterate all
Worksheet sheet = workbook.addSheet("Summary");
workbook.recalculate();                        // explicit formula evaluation
```

**Cell API:**
```java
Cell cell = sheet.cell("A1");         // A1 notation
Cell cell = sheet.cell(1, 1);         // row, col (1-based)

cell.setValue(42);
cell.setValue("Hello");
cell.setValue(LocalDate.now());
cell.setFormula("=SUM(A2:A10)");
cell.setStyle(style);

CellValue val   = cell.getValue();
String formula  = cell.getFormula();
CellStyle style = cell.getStyle();
```

Supported: shared strings, styles (font/fill/border/number format), merged cells, named ranges, multiple sheets.

### Style Model

```java
record CellStyle(Font font, Fill fill, Border border, NumberFormat numberFormat) {}
```

Immutable record — structural equality via `equals`/`hashCode`. Writer deduplicates `CellStyle` instances into the XLSX shared style table automatically. No style index leaks into public API.

**NumberFormat:**
```java
// predefined constants
NumberFormat.GENERAL
NumberFormat.INTEGER
NumberFormat.DECIMAL_2
NumberFormat.PERCENTAGE
NumberFormat.DATE_SHORT
NumberFormat.DATE_LONG
NumberFormat.DATETIME
NumberFormat.CURRENCY

// escape hatch
NumberFormat.custom("#,##0.000")
```

### OOXML I/O

| Operation | Strategy |
|-----------|----------|
| Read | StAX streaming — low memory, handles 1M+ row files |
| Write | StAX streaming — never build full DOM in memory |

Read pipeline:
```
ZIP entry → StAX cursor → shared strings → style table → sheet rows → Cell objects
```

Write pipeline:
```
Workbook model → CellStyle dedup → shared strings dedup → StAX output per sheet → ZIP assembly
```

v1: in-memory `Workbook` only. Row-by-row streaming API is v2.

---

## excel4j-formula

### Pipeline

```
String formula → Tokenizer → Token[] → Parser → FormulaNode (AST) → Evaluator → CellValue
```

### AST

```java
sealed interface FormulaNode permits
    NumberLiteral, TextLiteral, BoolLiteral, ErrorLiteral,
    CellRefNode, CellRangeNode,
    BinaryOp, UnaryOp,
    FunctionCall, NameRef {}

// absoluteness lives in the AST node, not in CellRef
record CellRefNode(CellRef ref, boolean rowAbsolute, boolean colAbsolute)
    implements FormulaNode {}
```

`CellRef` stays a pure coordinate. Absoluteness (`$A$1`, `$A1`, `A$1`) is a formula concern only.

### Function Registry

```java
interface ExcelFunction {
    CellValue evaluate(List<CellValue> args, EvalContext ctx);
}
```

v1 ships ~80 most-used functions: SUM, IF, VLOOKUP, INDEX, MATCH, TEXT, LEFT, RIGHT, MID, LEN, TRIM, DATE, TODAY, NOW, YEAR, MONTH, DAY, SUMIF, COUNTIF, AVERAGEIF, IFERROR, ISBLANK, AND, OR, NOT, MIN, MAX, ROUND, ABS, INT, MOD, CONCATENATE, plus common financial and statistical functions.
Extensible — users register custom functions.

### EvalContext

```java
interface EvalContext {
    CellValue resolve(CellRef ref);
    CellValue resolve(WorksheetName sheet, CellRef ref);
}
```

### Formula Evaluation

Triggered explicitly: `workbook.recalculate()`.
Circular references: affected cells get `ErrorValue(ErrorType.CIRCULAR_REF)`, rest of workbook continues evaluating normally.
Array formulas: not evaluated in v1 — cells with array formulas return their cached stored value.

### Error Types

`#DIV/0!`, `#VALUE!`, `#REF!`, `#NAME?`, `#N/A`, `#NULL!`, `#NUM!`, `#CIRCULAR_REF` — all `ErrorValue(ErrorType)`. Full Excel error propagation semantics.

---

## excel4j-report

### Tag Syntax

Fixed delimiter `<#...>` — matches FlexCelReport for zero migration friction.

| Tag | Meaning |
|-----|---------|
| `<#FieldName>` | Simple value substitution |
| `<#=Expression>` | Evaluated via formula engine (`excel4j-formula`) |
| `<#BandName>` | Band row start (repeating group) |
| `<#/BandName>` | Band row end |
| `<#if(expr)>` | Conditional row/block — expr evaluated via formula engine |
| `<#img(FieldName)>` | Image substitution |

### Band Expansion

Template rows marked with band tags expanded per datasource record. Engine:
1. Inserts rows for each record
2. Shifts all subsequent cells and rows
3. Patches cell references and formula ranges — relative refs shift, absolute refs (`$A$1`) stay fixed (resolved from `CellRefNode` absoluteness flags in AST)

Nested bands supported (master → detail → sub-detail).

### DataSource API

```java
Excel.report(templatePath)
     .bind("Orders", List.of(order1, order2))   // List<Record>, List<Map>, List<JavaBean>
     .bind("Summary", summaryRecord)             // single value binding
     .generate(outputPath);
```

Custom `DataSource` interface available for non-standard sources.

### Engine Pipeline

```
Load template workbook
→ Scan cells for tags
→ Build band tree (BandType sealed internal AST)
→ Expand bands against datasource (row insertion + ref patching)
→ Substitute remaining scalar tags
→ Evaluate <#=Expression> tags via formula engine
→ Recalculate formula ranges
→ Write output workbook
```

---

## excel4j-render (v2 stub)

Placeholder module. No implementation in v1. Dependency: `excel4j-core` only.

---

## Error Handling

Unchecked exception hierarchy — no checked exceptions in public API:

```
ExcelException (RuntimeException)
├── ExcelReadException
├── ExcelWriteException
├── FormulaParseException
├── FormulaEvalException
└── TemplateException
```

---

## Testing

| Module | Approach |
|--------|----------|
| `core` | Round-trip: write XLSX → read back → assert. Fixture files for edge cases (merged cells, styles, dates, 100k rows) |
| `formula` | Unit test per function. Parse → eval → assert. Error propagation, type coercion, circular ref |
| `report` | Template in + data → output XLSX. Assert expanded rows, shifted relative refs, fixed absolute refs, nested bands |

Coverage target: 80% minimum per module.
Framework: JUnit 5 + AssertJ.

---

## Build & Infrastructure

```
Java 21 (LTS minimum)
Maven multi-module
Full JPMS (module-info.java per module)
GitHub Actions CI (build + test on push/PR)
```

Parent POM enforces:
- `maven-compiler-plugin` — Java 21, no `--enable-preview`
- `maven-surefire-plugin` — JUnit 5
- `maven-enforcer-plugin` — no dependency convergence violations
- Checkstyle

Publishing: GitHub Packages → Maven Central when stable.

---

## JDK 21 Feature Usage

| Feature | Where used |
|---------|------------|
| Sealed interfaces | `CellValue`, `FormulaNode`, `BandType` (internal report AST) |
| Records | `CellRef`, `CellRange`, `WorksheetName`, `CellStyle`, `CellValue` subtypes |
| Pattern matching switch | Formula evaluator, cell value dispatch, band tree processing |
| Virtual threads | Parallel sheet writing in ZIP assembly for large multi-sheet workbooks |

No preview features. Stable APIs only.

---

## API Decisions Reference

| Decision | Choice |
|----------|--------|
| Row/col indexing | 1-based public API |
| Column addressing | Both int and A1 notation |
| Workbook model | Mutable |
| Formula evaluation | Explicit `workbook.recalculate()` |
| Date/time cells | `DateValue` / `DateTimeValue` as first-class `CellValue` permits |
| Error handling | Unchecked exceptions |
| Large file API | In-memory only (v1), streaming API is v2 |
| JPMS | Full `module-info.java` per module |
| Style model | Immutable `CellStyle` record + internal dedup |
| Ref absoluteness | In `CellRefNode` AST only, not in `CellRef` |
| Template delimiter | Fixed `<#...>` |
| Number formats | Predefined constants + `NumberFormat.custom()` |
| Entry point | Static `Excel` factory |
| Array formulas | Skip v1, return cached values |
| Sheet access | Both by name and by index |
| Cell API | Cell object chain `sheet.cell("A1").setValue(...)` |
| Circular refs | `ErrorValue(CIRCULAR_REF)` on affected cells, continue |

---

## Out of Scope (v1)

- PDF/image rendering (`excel4j-render` — v2)
- Row-by-row streaming read API (v2)
- Array formulas / dynamic arrays (v2)
- Charts (v2)
- Pivot tables (v2)
- Full 500-function Excel library (~80 functions in v1)
- Iterative circular reference calculation (never — niche feature)
- Macros/VBA (never)
