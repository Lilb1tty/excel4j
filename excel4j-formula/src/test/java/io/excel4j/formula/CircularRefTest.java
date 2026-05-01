package io.excel4j.formula;

import io.excel4j.core.model.*;
import io.excel4j.formula.ast.*;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import static org.assertj.core.api.Assertions.*;

class CircularRefTest {

    @Test
    void circularReferenceReturnsError() {
        Map<CellRef, CellValue> cells = new HashMap<>();
        cells.put(new CellRef(1, 1), new NumberValue(10.0));
        cells.put(new CellRef(1, 2), new NumberValue(20.0));

        Set<CellRef> visiting = new HashSet<>();

        EvalContext ctx = new EvalContext() {
            @Override
            public CellValue resolve(CellRef ref) {
                if (ref.row() == 1 && ref.col() == 3) {
                    // A3 references itself through the evaluator
                    return new Evaluator(this, new FunctionRegistry(), visiting)
                        .evaluate(new CellRefNode(ref, false, false));
                }
                return cells.getOrDefault(ref, new BlankValue());
            }
            @Override
            public CellValue resolve(WorksheetName sheet, CellRef ref) {
                return resolve(ref);
            }
        };

        // A3 = A3 (direct circular)
        Evaluator ev = new Evaluator(ctx, new FunctionRegistry(), visiting);
        CellValue result = ev.evaluate(new CellRefNode(new CellRef(1, 3), false, false));
        assertThat(result).isEqualTo(new ErrorValue(ErrorType.CIRCULAR_REF));
    }

    @Test
    void indirectCircularReference() {
        Map<CellRef, FormulaNode> formulas = new HashMap<>();
        formulas.put(new CellRef(1, 1), new CellRefNode(new CellRef(1, 2), false, false));
        formulas.put(new CellRef(1, 2), new CellRefNode(new CellRef(1, 1), false, false));

        Set<CellRef> visiting = new HashSet<>();

        EvalContext ctx = new EvalContext() {
            @Override
            public CellValue resolve(CellRef ref) {
                if (formulas.containsKey(ref)) {
                    return new Evaluator(this, new FunctionRegistry(), visiting)
                        .evaluate(formulas.get(ref));
                }
                return new BlankValue();
            }
            @Override
            public CellValue resolve(WorksheetName sheet, CellRef ref) {
                return resolve(ref);
            }
        };

        Evaluator ev = new Evaluator(ctx, new FunctionRegistry(), visiting);
        assertThat(ev.evaluate(formulas.get(new CellRef(1, 1))))
            .isEqualTo(new ErrorValue(ErrorType.CIRCULAR_REF));
    }
}
