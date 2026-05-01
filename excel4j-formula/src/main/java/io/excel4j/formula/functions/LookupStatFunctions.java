package io.excel4j.formula.functions;

import io.excel4j.core.model.*;
import io.excel4j.formula.EvalContext;
import io.excel4j.formula.Evaluator;
import io.excel4j.formula.FunctionRegistry;

import java.util.List;

public final class LookupStatFunctions {

    private LookupStatFunctions() {}

    public static void registerAll(FunctionRegistry reg) {
        reg.register("VLOOKUP", LookupStatFunctions::vlookup);
        reg.register("INDEX", LookupStatFunctions::index);
        reg.register("MATCH", LookupStatFunctions::match);
        reg.register("COUNTA", LookupStatFunctions::counta);
        reg.register("COUNTIF", LookupStatFunctions::countif);
        reg.register("SUMIF", LookupStatFunctions::sumif);
        reg.register("AVERAGEIF", LookupStatFunctions::averageif);
    }

    static CellValue vlookup(List<CellValue> args, EvalContext ctx) {
        CellValue lookup = args.get(0);
        int colIndex = (int) Evaluator.toNumber(args.get(2)) - 1;
        boolean exact = args.size() <= 3 || toBool(args.get(3));

        if (!(args.get(1) instanceof RangeValue(var range, var values))) {
            return new ErrorValue(ErrorType.VALUE);
        }
        int cols = range.last().col() - range.first().col() + 1;
        int rows = values.size() / cols;

        for (int row = 0; row < rows; row++) {
            CellValue cell = values.get(row * cols);
            if (exact ? equals(lookup, cell) : compare(lookup, cell) <= 0) {
                if (colIndex < cols && row * cols + colIndex < values.size()) {
                    return values.get(row * cols + colIndex);
                }
            }
        }
        return new ErrorValue(ErrorType.NA);
    }

    static CellValue index(List<CellValue> args, EvalContext ctx) {
        if (!(args.get(0) instanceof RangeValue(var range, var values))) {
            return args.size() > 2 ? args.get(0) : new ErrorValue(ErrorType.REF);
        }
        int row = (int) Evaluator.toNumber(args.get(1));
        int col = args.size() > 2 ? (int) Evaluator.toNumber(args.get(2)) : 1;
        int cols = range.last().col() - range.first().col() + 1;
        int idx = (row - 1) * cols + (col - 1);
        if (idx < 0 || idx >= values.size()) return new ErrorValue(ErrorType.REF);
        return values.get(idx);
    }

    static CellValue match(List<CellValue> args, EvalContext ctx) {
        CellValue lookup = args.get(0);
        if (!(args.get(1) instanceof RangeValue(var range, var values))) {
            return new ErrorValue(ErrorType.NA);
        }
        int matchType = args.size() > 2 ? (int) Evaluator.toNumber(args.get(2)) : 1;
        for (int i = 0; i < values.size(); i++) {
            int cmp = compare(lookup, values.get(i));
            if (matchType == 0 && cmp == 0) return new NumberValue(i + 1);
            if (matchType == 1 && cmp <= 0) return new NumberValue(Math.max(1, i));
            if (matchType == -1 && cmp >= 0) return new NumberValue(Math.max(1, i));
        }
        return new ErrorValue(ErrorType.NA);
    }

    static CellValue counta(List<CellValue> args, EvalContext ctx) {
        int c = 0;
        for (CellValue v : args) {
            if (!(v instanceof BlankValue)) c++;
        }
        return new NumberValue(c);
    }

    static CellValue countif(List<CellValue> args, EvalContext ctx) {
        if (!(args.get(0) instanceof RangeValue(var range, var values))) {
            return new NumberValue(0);
        }
        CellValue criteria = args.get(1);
        int c = 0;
        for (CellValue v : values) {
            if (matchesCriteria(v, criteria)) c++;
        }
        return new NumberValue(c);
    }

    static CellValue sumif(List<CellValue> args, EvalContext ctx) {
        if (!(args.get(0) instanceof RangeValue(var range, var values))) {
            return new NumberValue(0);
        }
        CellValue criteria = args.get(1);
        List<CellValue> sumRange = args.size() > 2 && args.get(2) instanceof RangeValue(var r, var sr)
            ? sr : values;
        double sum = 0;
        for (int i = 0; i < Math.min(values.size(), sumRange.size()); i++) {
            if (matchesCriteria(values.get(i), criteria)) {
                sum += Evaluator.toNumber(sumRange.get(i));
            }
        }
        return new NumberValue(sum);
    }

    static CellValue averageif(List<CellValue> args, EvalContext ctx) {
        if (!(args.get(0) instanceof RangeValue(var range, var values))) {
            return new ErrorValue(ErrorType.DIV_BY_ZERO);
        }
        CellValue criteria = args.get(1);
        List<CellValue> avgRange = args.size() > 2 && args.get(2) instanceof RangeValue(var r, var ar)
            ? ar : values;
        double sum = 0;
        int c = 0;
        for (int i = 0; i < Math.min(values.size(), avgRange.size()); i++) {
            if (matchesCriteria(values.get(i), criteria)) {
                sum += Evaluator.toNumber(avgRange.get(i));
                c++;
            }
        }
        if (c == 0) return new ErrorValue(ErrorType.DIV_BY_ZERO);
        return new NumberValue(sum / c);
    }

    static boolean matchesCriteria(CellValue value, CellValue criteria) {
        if (criteria instanceof TextValue(var s) && s.startsWith(">")) {
            double num = Evaluator.toNumber(value);
            double crit = Double.parseDouble(s.substring(1));
            return num > crit;
        }
        if (criteria instanceof TextValue(var s) && s.startsWith("<")) {
            double num = Evaluator.toNumber(value);
            double crit = Double.parseDouble(s.substring(1));
            return num < crit;
        }
        return equals(value, criteria);
    }

    static boolean equals(CellValue a, CellValue b) {
        if (a instanceof NumberValue(var na) && b instanceof NumberValue(var nb)) return na == nb;
        return Evaluator.toText(a).equalsIgnoreCase(Evaluator.toText(b));
    }

    static boolean toBool(CellValue v) {
        return switch (v) {
            case BooleanValue(var b) -> b;
            case NumberValue(var n) -> n != 0;
            case TextValue(var s) -> !s.isEmpty();
            case BlankValue() -> false;
            default -> false;
        };
    }

    static int compare(CellValue a, CellValue b) {
        if (a instanceof NumberValue(var na) && b instanceof NumberValue(var nb)) {
            return Double.compare(na, nb);
        }
        return Evaluator.toText(a).compareTo(Evaluator.toText(b));
    }
}
