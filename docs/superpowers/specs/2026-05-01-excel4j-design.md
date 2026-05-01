# excel4j Design Spec
**Date:** 2026-05-01
**Status:** Approved

## Summary

Java reimplementation of FlexCel (.NET) for JDK 21. FlexCel-inspired API ergonomics — template engine, formula evaluator, clean read/write model — built from scratch with zero external runtime dependencies. Open source, Apache 2.0, published on GitHub.

Not a port of .NET source (proprietary). Not a wrapper over Apache POI. Pure JDK implementation.

---

## Goals

- v1: Read/write XLSX + formula evaluation + template-based report generation
- v2: PDF/image rendering
- Zero runtime dependencies (JDK 21 only)
- FlexCel-quality developer experience — the gap between FlexCel and EasyExcel/POI is the motivation
- Apache 2.0, Maven Central

---

## Module Architecture

Maven multi-module. Strict one-directional dependency graph, no cycles.

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

Users depend on the highest module they need:
- XLSX I/O only → `excel4j-core`
- Formula evaluation → `excel4j-formula`
- Template reports → `excel4j-report`

---

## excel4j-core

### Cell Value Model

```java
sealed interface CellValue
    permits TextValue, NumberValue, BooleanValue, ErrorValue, BlankValue {}

record TextValue(String value)     implements CellValue {}
record NumberValue(double value)   implements CellValue {}
record BooleanValue(boolean value) implements CellValue {}
record ErrorValue(ErrorType type)  implements CellValue {}
record BlankValue()                implements CellValue {}
```

Pattern matching via `switch (node)` — no `instanceof` chains, no type enum dispatch.

### Key Records

```java
record CellRef(int row, int col) {}           // 0-based internally
record CellRange(CellRef first, CellRef last) {}
record WorksheetName(String value) {}
```

### Workbook Model

```
Workbook
└── Worksheet[]
    └── Row[]
        └── Cell (ref, value, formula?, style)
```

Supported: shared strings, styles (font/fill/border/number format), merged cells, named ranges, multiple sheets.

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
Workbook model → shared strings dedup → StAX output per sheet → ZIP assembly
```

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
```

### Function Registry

```java
interface ExcelFunction {
    CellValue evaluate(List<CellValue> args, EvalContext ctx);
}
```

v1 ships ~80 most-used functions: SUM, IF, VLOOKUP, INDEX/MATCH, TEXT, DATE family, string functions. Extensible — users register custom functions.

### EvalContext

```java
interface EvalContext {
    CellValue resolve(CellRef ref);
    CellValue resolve(WorksheetName sheet, CellRef ref);
}
```

Plugs into live `Workbook` or test mocks.

### Error Types

`#DIV/0!`, `#VALUE!`, `#REF!`, `#NAME?`, `#N/A`, `#NULL!`, `#NUM!` — all map to `ErrorValue(ErrorType)`. Full Excel error propagation semantics.

---

## excel4j-report

### Tag Syntax

| Tag | Meaning |
|-----|---------|
| `<#FieldName>` | Simple value substitution |
| `<#=Expression>` | Evaluated via formula engine (uses `excel4j-formula` evaluator) |
| `<#BandName>` | Band row start (repeating group) |
| `<#/BandName>` | Band row end |
| `<#if(expr)>` | Conditional row/block — expr evaluated via formula engine |
| `<#img(FieldName)>` | Image substitution |

### Band Expansion

Template rows marked with band tags are expanded per datasource record. Engine:
1. Inserts rows for each record
2. Shifts all subsequent cells and rows
3. Patches cell references and formula ranges automatically

Nested bands supported (master → detail → sub-detail).

### DataSource API

```java
ReportBuilder.from(templatePath)
    .bind("Orders", List.of(order1, order2))
    .bind("Summary", summaryRecord)
    .generate(outputPath);
```

Supported datasource types: `List<Map<String,Object>>`, `List<Record>`, `List<JavaBean>`, custom `DataSource` interface.

### Engine Pipeline

```
Load template workbook
→ Scan cells for tags
→ Build band tree
→ Expand bands against datasource (row insertion + ref patching)
→ Substitute remaining scalar tags
→ Recalculate formula ranges
→ Write output workbook
```

---

## excel4j-render (v2 stub)

Placeholder module. No implementation in v1. Dependency: `excel4j-core` only.

---

## Testing

| Module | Approach |
|--------|----------|
| `core` | Round-trip: write XLSX → read back → assert. Fixture files for edge cases (merged cells, styles, 100k rows) |
| `formula` | Unit test per function. Parse → eval → assert. Excel error/coercion edge cases |
| `report` | Template in + data → output XLSX. Assert expanded rows, shifted formulas, nested bands |

Coverage target: 80% minimum per module.
Framework: JUnit 5 + AssertJ.

---

## Build & Infrastructure

```
Java 21 (LTS minimum)
Maven multi-module
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
| Records | `CellRef`, `CellRange`, `WorksheetName` |
| Pattern matching switch | Formula evaluator, cell value dispatch |
| Virtual threads | Async OOXML write for large workbooks |

No preview features. Stable APIs only.

---

## Out of Scope (v1)

- PDF/image rendering (`excel4j-render` — v2)
- Charts (v2)
- Pivot tables (v2)
- Full 500-function Excel function library (~80 functions in v1, rest post-v1)
- Macros/VBA (never — out of scope)
