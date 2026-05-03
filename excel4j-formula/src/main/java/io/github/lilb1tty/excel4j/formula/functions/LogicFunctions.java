package io.github.lilb1tty.excel4j.formula.functions;

import io.github.lilb1tty.excel4j.core.model.*;
import io.github.lilb1tty.excel4j.formula.EvalContext;
import io.github.lilb1tty.excel4j.formula.FunctionRegistry;

import java.util.List;

public final class LogicFunctions {

    private LogicFunctions() {}

    public static void registerAll(FunctionRegistry reg) {
        reg.register("IF", LogicFunctions::ifFunc);
        reg.register("AND", LogicFunctions::and);
        reg.register("OR", LogicFunctions::or);
        reg.register("NOT", LogicFunctions::not);
        reg.register("IFERROR", LogicFunctions::ifError);
        reg.register("ISBLANK", LogicFunctions::isBlank);
        reg.register("ISNUMBER", LogicFunctions::isNumber);
        reg.register("ISTEXT", LogicFunctions::isText);
        reg.register("ISLOGICAL", LogicFunctions::isLogical);
        reg.register("ISERROR", LogicFunctions::isError);
        reg.register("XOR", LogicFunctions::xor);
        reg.register("IFS", LogicFunctions::ifs);
        reg.register("SWITCH", LogicFunctions::switchFunc);
        reg.register("IFNA", LogicFunctions::ifna);
    }

    static CellValue ifFunc(List<CellValue> args, EvalContext ctx) {
        boolean condition = toBool(args.get(0));
        if (condition) {
            return args.size() > 1 ? args.get(1) : new BooleanValue(false);
        } else {
            return args.size() > 2 ? args.get(2) : new BooleanValue(false);
        }
    }

    static CellValue and(List<CellValue> args, EvalContext ctx) {
        for (CellValue v : args) {
            if (!toBool(v)) return new BooleanValue(false);
        }
        return new BooleanValue(true);
    }

    static CellValue or(List<CellValue> args, EvalContext ctx) {
        for (CellValue v : args) {
            if (toBool(v)) return new BooleanValue(true);
        }
        return new BooleanValue(false);
    }

    static CellValue not(List<CellValue> args, EvalContext ctx) {
        return new BooleanValue(!toBool(args.get(0)));
    }

    static CellValue ifError(List<CellValue> args, EvalContext ctx) {
        if (args.get(0) instanceof ErrorValue) {
            return args.size() > 1 ? args.get(1) : new BlankValue();
        }
        return args.get(0);
    }

    static CellValue isBlank(List<CellValue> args, EvalContext ctx) {
        return new BooleanValue(args.get(0) instanceof BlankValue);
    }

    static CellValue isNumber(List<CellValue> args, EvalContext ctx) {
        return new BooleanValue(args.get(0) instanceof NumberValue);
    }

    static CellValue isText(List<CellValue> args, EvalContext ctx) {
        return new BooleanValue(args.get(0) instanceof TextValue);
    }

    static CellValue isLogical(List<CellValue> args, EvalContext ctx) {
        return new BooleanValue(args.get(0) instanceof BooleanValue);
    }

    static CellValue isError(List<CellValue> args, EvalContext ctx) {
        return new BooleanValue(args.get(0) instanceof ErrorValue);
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

    static CellValue xor(List<CellValue> args, EvalContext ctx) {
        boolean result = false;
        for (CellValue v : args) result ^= toBool(v);
        return new BooleanValue(result);
    }

    static CellValue ifs(List<CellValue> args, EvalContext ctx) {
        for (int i = 0; i + 1 < args.size(); i += 2) {
            if (toBool(args.get(i))) return args.get(i + 1);
        }
        return new ErrorValue(ErrorType.NA);
    }

    static CellValue switchFunc(List<CellValue> args, EvalContext ctx) {
        CellValue expr = args.get(0);
        // args: expr, val1, result1, val2, result2, ..., [default]
        // default present when (args.size() - 1) is odd (i.e., args.size() is even)
        boolean hasDefault = args.size() % 2 == 0;
        int pairEnd = hasDefault ? args.size() - 1 : args.size();
        for (int i = 1; i + 1 < pairEnd; i += 2) {
            if (LookupStatFunctions.equals(expr, args.get(i))) return args.get(i + 1);
        }
        return hasDefault ? args.get(args.size() - 1) : new ErrorValue(ErrorType.NA);
    }

    static CellValue ifna(List<CellValue> args, EvalContext ctx) {
        CellValue v = args.get(0);
        if (v instanceof ErrorValue ev && ev.type() == ErrorType.NA) {
            return args.size() > 1 ? args.get(1) : new BlankValue();
        }
        return v;
    }
}
