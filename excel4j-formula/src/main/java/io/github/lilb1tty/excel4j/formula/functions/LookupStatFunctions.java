package io.github.lilb1tty.excel4j.formula.functions;

import io.github.lilb1tty.excel4j.core.model.*;
import io.github.lilb1tty.excel4j.formula.EvalContext;
import io.github.lilb1tty.excel4j.formula.Evaluator;
import io.github.lilb1tty.excel4j.formula.FunctionRegistry;

import java.util.ArrayList;
import java.util.Collections;
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
        reg.register("HLOOKUP", LookupStatFunctions::hlookup);
        reg.register("CHOOSE", LookupStatFunctions::choose);
        reg.register("LARGE", LookupStatFunctions::large);
        reg.register("SMALL", LookupStatFunctions::small);
        reg.register("RANK", LookupStatFunctions::rank);
        reg.register("MEDIAN", LookupStatFunctions::median);
        reg.register("MODE", LookupStatFunctions::mode);
        reg.register("STDEV", LookupStatFunctions::stdev);
        reg.register("VAR", LookupStatFunctions::variance);
        reg.register("COUNTIFS", LookupStatFunctions::countifs);
        reg.register("SUMIFS", LookupStatFunctions::sumifs);
        reg.register("AVERAGEIFS", LookupStatFunctions::averageifs);
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

    static CellValue hlookup(List<CellValue> args, EvalContext ctx) {
        CellValue lookup = args.get(0);
        if (!(args.get(1) instanceof RangeValue(var range, var values))) {
            return new ErrorValue(ErrorType.VALUE);
        }
        int rowIndex = (int) Evaluator.toNumber(args.get(2)) - 1;
        boolean exact = args.size() <= 3 || toBool(args.get(3));
        int cols = range.last().col() - range.first().col() + 1;
        for (int col = 0; col < cols; col++) {
            CellValue cell = values.get(col);
            if (exact ? equals(lookup, cell) : compare(lookup, cell) <= 0) {
                int idx = rowIndex * cols + col;
                if (idx < values.size()) {
                    return values.get(idx);
                }
            }
        }
        return new ErrorValue(ErrorType.NA);
    }

    static CellValue choose(List<CellValue> args, EvalContext ctx) {
        int idx = (int) Evaluator.toNumber(args.get(0));
        if (idx < 1 || idx >= args.size()) {
            return new ErrorValue(ErrorType.VALUE);
        }
        return args.get(idx);
    }

    static CellValue large(List<CellValue> args, EvalContext ctx) {
        int k = (int) Evaluator.toNumber(args.get(args.size() - 1));
        List<Double> nums = new ArrayList<>();
        for (int i = 0; i < args.size() - 1; i++) {
            if (args.get(i) instanceof NumberValue(var n)) {
                nums.add(n);
            }
        }
        if (k < 1 || k > nums.size()) {
            return new ErrorValue(ErrorType.NUM);
        }
        nums.sort(Collections.reverseOrder());
        return new NumberValue(nums.get(k - 1));
    }

    static CellValue small(List<CellValue> args, EvalContext ctx) {
        int k = (int) Evaluator.toNumber(args.get(args.size() - 1));
        List<Double> nums = new ArrayList<>();
        for (int i = 0; i < args.size() - 1; i++) {
            if (args.get(i) instanceof NumberValue(var n)) {
                nums.add(n);
            }
        }
        if (k < 1 || k > nums.size()) {
            return new ErrorValue(ErrorType.NUM);
        }
        Collections.sort(nums);
        return new NumberValue(nums.get(k - 1));
    }

    static CellValue rank(List<CellValue> args, EvalContext ctx) {
        double number = Evaluator.toNumber(args.get(0));
        List<Double> nums = new ArrayList<>();
        for (int i = 1; i < args.size(); i++) {
            if (args.get(i) instanceof NumberValue(var n)) {
                nums.add(n);
            }
        }
        nums.sort(Collections.reverseOrder());
        int r = nums.indexOf(number);
        return r < 0 ? new ErrorValue(ErrorType.NA) : new NumberValue(r + 1.0);
    }

    static CellValue median(List<CellValue> args, EvalContext ctx) {
        List<Double> nums = new ArrayList<>();
        for (CellValue v : args) {
            if (v instanceof NumberValue(var n)) {
                nums.add(n);
            }
        }
        if (nums.isEmpty()) {
            return new ErrorValue(ErrorType.NUM);
        }
        Collections.sort(nums);
        int mid = nums.size() / 2;
        return nums.size() % 2 == 1 ? new NumberValue(nums.get(mid))
            : new NumberValue((nums.get(mid - 1) + nums.get(mid)) / 2.0);
    }

    static CellValue mode(List<CellValue> args, EvalContext ctx) {
        java.util.Map<Double, Integer> freq = new java.util.LinkedHashMap<>();
        for (CellValue v : args) {
            if (v instanceof NumberValue(var n)) {
                freq.merge(n, 1, Integer::sum);
            }
        }
        return freq.entrySet().stream().max(java.util.Map.Entry.comparingByValue())
            .map(e -> (CellValue) new NumberValue(e.getKey()))
            .orElse(new ErrorValue(ErrorType.NA));
    }

    static CellValue stdev(List<CellValue> args, EvalContext ctx) {
        List<Double> nums = new ArrayList<>();
        for (CellValue v : args) {
            if (v instanceof NumberValue(var n)) {
                nums.add(n);
            }
        }
        if (nums.size() < 2) {
            return new ErrorValue(ErrorType.DIV_BY_ZERO);
        }
        double mean = nums.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double variance = nums.stream().mapToDouble(n -> (n - mean) * (n - mean)).sum()
            / (nums.size() - 1);
        return new NumberValue(Math.sqrt(variance));
    }

    static CellValue variance(List<CellValue> args, EvalContext ctx) {
        List<Double> nums = new ArrayList<>();
        for (CellValue v : args) {
            if (v instanceof NumberValue(var n)) {
                nums.add(n);
            }
        }
        if (nums.size() < 2) {
            return new ErrorValue(ErrorType.DIV_BY_ZERO);
        }
        double mean = nums.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        return new NumberValue(
            nums.stream().mapToDouble(n -> (n - mean) * (n - mean)).sum() / (nums.size() - 1));
    }

    static CellValue countifs(List<CellValue> args, EvalContext ctx) {
        return countif(args, ctx);
    }

    static CellValue sumifs(List<CellValue> args, EvalContext ctx) {
        return sumif(args, ctx);
    }

    static CellValue averageifs(List<CellValue> args, EvalContext ctx) {
        return averageif(args, ctx);
    }
}
