package io.excel4j.formula;

import io.excel4j.core.model.*;
import io.excel4j.formula.ast.*;

import java.util.ArrayList;
import java.util.List;

public class Evaluator {

    private final EvalContext ctx;
    private final FunctionRegistry functions;

    public Evaluator(EvalContext ctx) {
        this.ctx = ctx;
        this.functions = new FunctionRegistry();
    }

    public Evaluator(EvalContext ctx, FunctionRegistry functions) {
        this.ctx = ctx;
        this.functions = functions;
    }

    public CellValue evaluate(FormulaNode node) {
        return switch (node) {
            case NumberLiteral(var v) -> new NumberValue(v);
            case TextLiteral(var v) -> new TextValue(v);
            case BoolLiteral(var v) -> new BooleanValue(v);
            case ErrorLiteral(var t) -> new ErrorValue(t);
            case CellRefNode(var ref, var rowAbs, var colAbs) -> ctx.resolve(ref);
            case CellRangeNode(var range) -> evaluateRange(range);
            case BinaryOp(var op, var left, var right) -> evaluateBinary(op, left, right);
            case UnaryOp(var op, var operand) -> evaluateUnary(op, operand);
            case FunctionCall(var name, var args) -> evaluateFunction(name, args);
            case NameRef(var name) -> new ErrorValue(ErrorType.NAME);
        };
    }

    private CellValue evaluateRange(CellRange range) {
        List<CellValue> values = new ArrayList<>();
        for (int row = range.first().row(); row <= range.last().row(); row++) {
            for (int col = range.first().col(); col <= range.last().col(); col++) {
                values.add(ctx.resolve(new CellRef(row, col)));
            }
        }
        return new RangeValue(range, values);
    }

    private CellValue evaluateBinary(Operator op, FormulaNode left, FormulaNode right) {
        CellValue lv = evaluate(left);
        CellValue rv = evaluate(right);

        // error propagation
        if (lv instanceof ErrorValue(var ignored)) return lv;
        if (rv instanceof ErrorValue(var ignored)) return rv;

        return switch (op) {
            case ADD -> arithmetic(lv, rv, (a, b) -> a + b);
            case SUBTRACT -> arithmetic(lv, rv, (a, b) -> a - b);
            case MULTIPLY -> arithmetic(lv, rv, (a, b) -> a * b);
            case DIVIDE -> {
                double b = toNumber(rv);
                if (b == 0) yield new ErrorValue(ErrorType.DIV_BY_ZERO);
                yield arithmetic(lv, rv, (a, bb) -> a / bb);
            }
            case POWER -> arithmetic(lv, rv, Math::pow);
            case CONCAT -> new TextValue(toText(lv) + toText(rv));
            case EQ -> new BooleanValue(compare(lv, rv) == 0);
            case NE -> new BooleanValue(compare(lv, rv) != 0);
            case LT -> new BooleanValue(compare(lv, rv) < 0);
            case GT -> new BooleanValue(compare(lv, rv) > 0);
            case LE -> new BooleanValue(compare(lv, rv) <= 0);
            case GE -> new BooleanValue(compare(lv, rv) >= 0);
        };
    }

    private CellValue evaluateUnary(UnaryOperator op, FormulaNode operand) {
        CellValue v = evaluate(operand);
        if (v instanceof ErrorValue) return v;
        return switch (op) {
            case NEGATE -> new NumberValue(-toNumber(v));
            case PERCENT -> new NumberValue(toNumber(v) / 100.0);
        };
    }

    private CellValue evaluateFunction(String name, List<FormulaNode> args) {
        ExcelFunction fn = functions.find(name);
        if (fn == null) return new ErrorValue(ErrorType.NAME);
        List<CellValue> evaluated = new ArrayList<>();
        for (FormulaNode arg : args) {
            CellValue v = evaluate(arg);
            if (v instanceof RangeValue(var range, var values)) {
                evaluated.addAll(values);
            } else {
                evaluated.add(v);
            }
        }
        return fn.evaluate(evaluated, ctx);
    }

    private CellValue arithmetic(CellValue a, CellValue b, java.util.function.DoubleBinaryOperator op) {
        return new NumberValue(op.applyAsDouble(toNumber(a), toNumber(b)));
    }

    static double toNumber(CellValue v) {
        return switch (v) {
            case NumberValue(var n) -> n;
            case BooleanValue(var b) -> b ? 1 : 0;
            case BlankValue() -> 0;
            default -> 0;
        };
    }

    static String toText(CellValue v) {
        return switch (v) {
            case TextValue(var s) -> s;
            case NumberValue(var n) -> String.valueOf(n);
            case BooleanValue(var b) -> b ? "TRUE" : "FALSE";
            case BlankValue() -> "";
            case ErrorValue(var t) -> t.toExcelString();
            default -> "";
        };
    }

    static int compare(CellValue a, CellValue b) {
        if (a instanceof NumberValue(var na) && b instanceof NumberValue(var nb)) {
            return Double.compare(na, nb);
        }
        return toText(a).compareTo(toText(b));
    }
}
