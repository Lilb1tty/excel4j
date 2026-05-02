package io.excel4j.formula.functions;

import io.excel4j.core.model.*;
import io.excel4j.formula.EvalContext;
import io.excel4j.formula.Evaluator;
import io.excel4j.formula.FunctionRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

public final class ArrayFunctions {

    private ArrayFunctions() {}

    public static void registerAll(FunctionRegistry reg) {
        reg.register("SEQUENCE", ArrayFunctions::sequence);
        reg.register("UNIQUE", ArrayFunctions::unique);
        reg.register("SORT", ArrayFunctions::sort);
    }

    static CellValue sequence(List<CellValue> args, EvalContext ctx) {
        int rows = (int) Evaluator.toNumber(args.get(0));
        int cols = args.size() > 1 ? (int) Evaluator.toNumber(args.get(1)) : 1;
        double start = args.size() > 2 ? Evaluator.toNumber(args.get(2)) : 1;
        double step = args.size() > 3 ? Evaluator.toNumber(args.get(3)) : 1;
        List<CellValue> values = new ArrayList<>();
        double current = start;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                values.add(new NumberValue(current));
                current += step;
            }
        }
        CellRange range = new CellRange(new CellRef(1, 1), new CellRef(rows, cols));
        return new RangeValue(range, values);
    }

    static CellValue unique(List<CellValue> args, EvalContext ctx) {
        LinkedHashSet<Double> seen = new LinkedHashSet<>();
        for (CellValue v : args) {
            seen.add(Evaluator.toNumber(v));
        }
        List<CellValue> values = new ArrayList<>();
        for (Double d : seen) values.add(new NumberValue(d));
        CellRange range = new CellRange(new CellRef(1, 1), new CellRef(1, values.size()));
        return new RangeValue(range, values);
    }

    static CellValue sort(List<CellValue> args, EvalContext ctx) {
        List<Double> nums = new ArrayList<>();
        for (CellValue v : args) nums.add(Evaluator.toNumber(v));
        Collections.sort(nums);
        List<CellValue> values = new ArrayList<>();
        for (Double d : nums) values.add(new NumberValue(d));
        CellRange range = new CellRange(new CellRef(1, 1), new CellRef(1, values.size()));
        return new RangeValue(range, values);
    }
}
