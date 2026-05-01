package io.excel4j.formula.functions;

import io.excel4j.core.model.*;
import io.excel4j.formula.EvalContext;
import io.excel4j.formula.FunctionRegistry;

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
}
