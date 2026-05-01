package io.excel4j.formula.functions;

import io.excel4j.core.model.*;
import io.excel4j.formula.EvalContext;
import io.excel4j.formula.Evaluator;
import io.excel4j.formula.FunctionRegistry;

import java.util.List;

public final class TextFunctions {

    private TextFunctions() {}

    public static void registerAll(FunctionRegistry reg) {
        reg.register("LEFT", TextFunctions::left);
        reg.register("RIGHT", TextFunctions::right);
        reg.register("MID", TextFunctions::mid);
        reg.register("LEN", TextFunctions::len);
        reg.register("TRIM", TextFunctions::trim);
        reg.register("CONCATENATE", TextFunctions::concatenate);
        reg.register("UPPER", TextFunctions::upper);
        reg.register("LOWER", TextFunctions::lower);
        reg.register("REPT", TextFunctions::rept);
        reg.register("FIND", TextFunctions::find);
        reg.register("SUBSTITUTE", TextFunctions::substitute);
    }

    static CellValue left(List<CellValue> args, EvalContext ctx) {
        String text = Evaluator.toText(args.get(0));
        int num = args.size() > 1 ? (int) Evaluator.toNumber(args.get(1)) : 1;
        return new TextValue(text.substring(0, Math.min(num, text.length())));
    }

    static CellValue right(List<CellValue> args, EvalContext ctx) {
        String text = Evaluator.toText(args.get(0));
        int num = args.size() > 1 ? (int) Evaluator.toNumber(args.get(1)) : 1;
        int start = Math.max(0, text.length() - num);
        return new TextValue(text.substring(start));
    }

    static CellValue mid(List<CellValue> args, EvalContext ctx) {
        String text = Evaluator.toText(args.get(0));
        int start = (int) Evaluator.toNumber(args.get(1)) - 1;
        int num = (int) Evaluator.toNumber(args.get(2));
        if (start < 0) start = 0;
        int end = Math.min(start + num, text.length());
        return new TextValue(text.substring(start, end));
    }

    static CellValue len(List<CellValue> args, EvalContext ctx) {
        return new NumberValue(Evaluator.toText(args.get(0)).length());
    }

    static CellValue trim(List<CellValue> args, EvalContext ctx) {
        return new TextValue(Evaluator.toText(args.get(0)).trim());
    }

    static CellValue concatenate(List<CellValue> args, EvalContext ctx) {
        StringBuilder sb = new StringBuilder();
        for (CellValue v : args) sb.append(Evaluator.toText(v));
        return new TextValue(sb.toString());
    }

    static CellValue upper(List<CellValue> args, EvalContext ctx) {
        return new TextValue(Evaluator.toText(args.get(0)).toUpperCase());
    }

    static CellValue lower(List<CellValue> args, EvalContext ctx) {
        return new TextValue(Evaluator.toText(args.get(0)).toLowerCase());
    }

    static CellValue rept(List<CellValue> args, EvalContext ctx) {
        String text = Evaluator.toText(args.get(0));
        int times = (int) Evaluator.toNumber(args.get(1));
        return new TextValue(text.repeat(Math.max(0, times)));
    }

    static CellValue find(List<CellValue> args, EvalContext ctx) {
        String find = Evaluator.toText(args.get(0));
        String within = Evaluator.toText(args.get(1));
        int start = args.size() > 2 ? (int) Evaluator.toNumber(args.get(2)) - 1 : 0;
        int idx = within.indexOf(find, start);
        if (idx < 0) return new ErrorValue(ErrorType.VALUE);
        return new NumberValue(idx + 1);
    }

    static CellValue substitute(List<CellValue> args, EvalContext ctx) {
        String text = Evaluator.toText(args.get(0));
        String oldText = Evaluator.toText(args.get(1));
        String newText = Evaluator.toText(args.get(2));
        if (args.size() > 3) {
            int instance = (int) Evaluator.toNumber(args.get(3));
            int pos = 0;
            int count = 0;
            while (pos < text.length()) {
                int idx = text.indexOf(oldText, pos);
                if (idx < 0) break;
                count++;
                if (count == instance) {
                    return new TextValue(text.substring(0, idx) + newText + text.substring(idx + oldText.length()));
                }
                pos = idx + 1;
            }
            return new TextValue(text);
        }
        return new TextValue(text.replace(oldText, newText));
    }
}
