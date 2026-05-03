# cellix — Claude Instructions

## Documentation Maintenance (REQUIRED)

After any new feature, module, or significant change, ALWAYS update:

1. **`README.md`** — Update the Features list, relevant Quick Start sections, and add an entry to the Changelog with today's date and a bullet-per-change.
2. **`docs/functions.md`** — When new formula functions are added, append them to the correct category section with full syntax, description, parameters, and an example.

Do this in the same commit as the implementation. Never leave docs out of date.

### Changelog entry format

```markdown
### vX.Y.Z — YYYY-MM-DD

**module-name**
- Brief description of what changed
```

## Project Context

- Maven multi-module Java 21 project, full JPMS
- Modules: `cellix-core`, `cellix-formula`, `cellix-report`, `cellix-render`
- Zero external runtime dependencies — JDK 21 only
- Apache 2.0, GitHub: https://github.com/Lilb1tty/cellix
- `docs/superpowers/` is gitignored (internal planning only)

## Dev Conventions

- All public row/col coordinates are 1-based
- `CellValue` is a sealed interface — use pattern matching switch, never instanceof chains
- Immutable style: `CellStyle` is a record, mutate via `withXxx()` copy methods
- Unchecked exceptions only — no checked exceptions in public API
- JPMS: exported packages only, no reflective access across module boundaries
- Entry point class: `Cellix` (was `Excel` pre-rename)
- Exception classes: `CellixException`, `CellixReadException`, `CellixWriteException`
- Custom function interface: `CellixFunction`
