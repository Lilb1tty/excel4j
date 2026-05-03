package io.github.lilb1tty.excel4j.formula;

import io.github.lilb1tty.excel4j.core.model.*;
import io.github.lilb1tty.excel4j.formula.ast.FormulaNode;

import java.util.HashSet;
import java.util.Set;

public class FormulaEvaluator {

    private final FunctionRegistry registry;

    public FormulaEvaluator() {
        this.registry = new FunctionRegistry();
    }

    public FormulaEvaluator(FunctionRegistry registry) {
        this.registry = registry;
    }

    public CellValue evaluate(String formula, EvalContext ctx) {
        FormulaNode ast = new Parser().parse(formula);
        return new Evaluator(ctx, registry, new HashSet<>()).evaluate(ast);
    }

    public java.util.function.BiFunction<Worksheet, Cell, CellValue> workbookEvaluator(Workbook workbook) {
        return (sheet, cell) -> {
            var visiting = new java.util.HashSet<CellRef>();
            var ctx = new WorkbookEvalContext(workbook, sheet, visiting, registry);
            var evaluator = new Evaluator(ctx, registry, visiting);
            return evaluator.evaluate(new Parser().parse(cell.getFormula()));
        };
    }

    private static class WorkbookEvalContext implements EvalContext {
        private final Workbook workbook;
        private final Worksheet currentSheet;
        private final java.util.Set<CellRef> visiting;
        private final FunctionRegistry registry;

        WorkbookEvalContext(Workbook workbook, Worksheet currentSheet,
                            java.util.Set<CellRef> visiting, FunctionRegistry registry) {
            this.workbook = workbook;
            this.currentSheet = currentSheet;
            this.visiting = visiting;
            this.registry = registry;
        }

        @Override
        public CellValue resolve(CellRef ref) {
            return resolveCell(currentSheet, ref);
        }

        @Override
        public CellValue resolve(WorksheetName sheetName, CellRef ref) {
            Worksheet sheet = workbook.sheet(sheetName.value());
            return resolveCell(sheet, ref);
        }

        private CellValue resolveCell(Worksheet sheet, CellRef ref) {
            Cell cell = sheet.cell(ref);
            if (cell.getFormula() != null && cell.getCachedValue() != null) {
                return cell.getCachedValue();
            }
            if (cell.getFormula() != null) {
                return new Evaluator(this, registry, visiting)
                    .evaluate(new Parser().parse(cell.getFormula()));
            }
            return cell.getValue();
        }
    }
}
