package io.github.lilb1tty.excel4j.formula.functions;

import io.github.lilb1tty.excel4j.core.model.*;
import io.github.lilb1tty.excel4j.formula.EvalContext;
import io.github.lilb1tty.excel4j.formula.Evaluator;
import io.github.lilb1tty.excel4j.formula.ExcelFunction;
import io.github.lilb1tty.excel4j.formula.FunctionRegistry;

import java.util.List;

public final class MathFunctions {

    private MathFunctions() {}

    public static void registerAll(FunctionRegistry reg) {
        reg.register("SUM", MathFunctions::sum);
        reg.register("PRODUCT", MathFunctions::product);
        reg.register("MIN", MathFunctions::min);
        reg.register("MAX", MathFunctions::max);
        reg.register("ABS", MathFunctions::abs);
        reg.register("ROUND", MathFunctions::round);
        reg.register("INT", MathFunctions::intFunc);
        reg.register("MOD", MathFunctions::mod);
        reg.register("POWER", MathFunctions::power);
        reg.register("SQRT", MathFunctions::sqrt);
        reg.register("COUNT", MathFunctions::count);
        reg.register("AVERAGE", MathFunctions::average);
        reg.register("CEILING", MathFunctions::ceiling);
        reg.register("FLOOR", MathFunctions::floor);
        reg.register("ROUNDUP", MathFunctions::roundUp);
        reg.register("ROUNDDOWN", MathFunctions::roundDown);
        reg.register("TRUNC", MathFunctions::trunc);
        reg.register("SIGN", MathFunctions::sign);
        reg.register("LOG", MathFunctions::log);
        reg.register("LOG10", MathFunctions::log10);
        reg.register("LN", MathFunctions::ln);
        reg.register("EXP", MathFunctions::exp);
        reg.register("PI", MathFunctions::pi);
        reg.register("RAND", MathFunctions::rand);
        reg.register("RANDBETWEEN", MathFunctions::randBetween);
        reg.register("FACT", MathFunctions::fact);
        reg.register("SUMPRODUCT", MathFunctions::sumProduct);
        reg.register("SUMSQ", MathFunctions::sumSq);
    }

    static CellValue sum(List<CellValue> args, EvalContext ctx) {
        double sum = 0;
        for (CellValue v : args) {
            if (v instanceof NumberValue(var n)) sum += n;
            else if (v instanceof BooleanValue(var b)) sum += b ? 1 : 0;
            else if (v instanceof BlankValue()) sum += 0;
            else if (v instanceof ErrorValue(var e)) return v;
        }
        return new NumberValue(sum);
    }

    static CellValue product(List<CellValue> args, EvalContext ctx) {
        double prod = 1;
        for (CellValue v : args) {
            if (v instanceof NumberValue(var n)) prod *= n;
            else if (v instanceof BooleanValue(var b)) prod *= b ? 1 : 0;
            else if (v instanceof BlankValue()) prod *= 0;
            else if (v instanceof ErrorValue(var e)) return v;
        }
        return new NumberValue(prod);
    }

    static CellValue min(List<CellValue> args, EvalContext ctx) {
        double min = Double.MAX_VALUE;
        boolean found = false;
        for (CellValue v : args) {
            if (v instanceof NumberValue(var n)) {
                if (!found || n < min) min = n;
                found = true;
            } else if (v instanceof ErrorValue(var e)) return v;
        }
        return found ? new NumberValue(min) : new NumberValue(0);
    }

    static CellValue max(List<CellValue> args, EvalContext ctx) {
        double max = -Double.MAX_VALUE;
        boolean found = false;
        for (CellValue v : args) {
            if (v instanceof NumberValue(var n)) {
                if (!found || n > max) max = n;
                found = true;
            } else if (v instanceof ErrorValue(var e)) return v;
        }
        return found ? new NumberValue(max) : new NumberValue(0);
    }

    static CellValue abs(List<CellValue> args, EvalContext ctx) {
        return new NumberValue(Math.abs(Evaluator.toNumber(args.get(0))));
    }

    static CellValue round(List<CellValue> args, EvalContext ctx) {
        double num = Evaluator.toNumber(args.get(0));
        int digits = (int) Evaluator.toNumber(args.get(1));
        double factor = Math.pow(10, digits);
        return new NumberValue(Math.round(num * factor) / factor);
    }

    static CellValue intFunc(List<CellValue> args, EvalContext ctx) {
        return new NumberValue(Math.floor(Evaluator.toNumber(args.get(0))));
    }

    static CellValue mod(List<CellValue> args, EvalContext ctx) {
        double n = Evaluator.toNumber(args.get(0));
        double d = Evaluator.toNumber(args.get(1));
        if (d == 0) return new ErrorValue(ErrorType.DIV_BY_ZERO);
        return new NumberValue(n % d);
    }

    static CellValue power(List<CellValue> args, EvalContext ctx) {
        return new NumberValue(Math.pow(
            Evaluator.toNumber(args.get(0)),
            Evaluator.toNumber(args.get(1))));
    }

    static CellValue sqrt(List<CellValue> args, EvalContext ctx) {
        double n = Evaluator.toNumber(args.get(0));
        if (n < 0) return new ErrorValue(ErrorType.NUM);
        return new NumberValue(Math.sqrt(n));
    }

    static CellValue count(List<CellValue> args, EvalContext ctx) {
        int c = 0;
        for (CellValue v : args) {
            if (v instanceof NumberValue || v instanceof BooleanValue) c++;
        }
        return new NumberValue(c);
    }

    static CellValue average(List<CellValue> args, EvalContext ctx) {
        double sum = 0;
        int c = 0;
        for (CellValue v : args) {
            if (v instanceof NumberValue(var n)) { sum += n; c++; }
            else if (v instanceof BooleanValue(var b)) { sum += b ? 1 : 0; c++; }
            else if (v instanceof ErrorValue(var e)) return v;
        }
        if (c == 0) return new ErrorValue(ErrorType.DIV_BY_ZERO);
        return new NumberValue(sum / c);
    }

    static CellValue ceiling(List<CellValue> args, EvalContext ctx) {
        double n = Evaluator.toNumber(args.get(0));
        double sig = Evaluator.toNumber(args.get(1));
        if (sig == 0) return new NumberValue(0);
        return new NumberValue(Math.ceil(n / sig) * sig);
    }

    static CellValue floor(List<CellValue> args, EvalContext ctx) {
        double n = Evaluator.toNumber(args.get(0));
        double sig = Evaluator.toNumber(args.get(1));
        if (sig == 0) return new NumberValue(0);
        return new NumberValue(Math.floor(n / sig) * sig);
    }

    static CellValue roundUp(List<CellValue> args, EvalContext ctx) {
        double n = Evaluator.toNumber(args.get(0));
        int digits = (int) Evaluator.toNumber(args.get(1));
        double factor = Math.pow(10, digits);
        double abs = Math.abs(n);
        return new NumberValue(Math.signum(n) * Math.ceil(abs * factor) / factor);
    }

    static CellValue roundDown(List<CellValue> args, EvalContext ctx) {
        double n = Evaluator.toNumber(args.get(0));
        int digits = (int) Evaluator.toNumber(args.get(1));
        double factor = Math.pow(10, digits);
        double abs = Math.abs(n);
        return new NumberValue(Math.signum(n) * Math.floor(abs * factor) / factor);
    }

    static CellValue trunc(List<CellValue> args, EvalContext ctx) {
        double n = Evaluator.toNumber(args.get(0));
        int digits = args.size() > 1 ? (int) Evaluator.toNumber(args.get(1)) : 0;
        double factor = Math.pow(10, digits);
        return new NumberValue((n >= 0 ? Math.floor(n * factor) : Math.ceil(n * factor)) / factor);
    }

    static CellValue sign(List<CellValue> args, EvalContext ctx) {
        double n = Evaluator.toNumber(args.get(0));
        return new NumberValue(n > 0 ? 1 : n < 0 ? -1 : 0);
    }

    static CellValue log(List<CellValue> args, EvalContext ctx) {
        double n = Evaluator.toNumber(args.get(0));
        double base = args.size() > 1 ? Evaluator.toNumber(args.get(1)) : 10;
        if (n <= 0) return new ErrorValue(ErrorType.NUM);
        return new NumberValue(Math.log(n) / Math.log(base));
    }

    static CellValue log10(List<CellValue> args, EvalContext ctx) {
        double n = Evaluator.toNumber(args.get(0));
        if (n <= 0) return new ErrorValue(ErrorType.NUM);
        return new NumberValue(Math.log10(n));
    }

    static CellValue ln(List<CellValue> args, EvalContext ctx) {
        double n = Evaluator.toNumber(args.get(0));
        if (n <= 0) return new ErrorValue(ErrorType.NUM);
        return new NumberValue(Math.log(n));
    }

    static CellValue exp(List<CellValue> args, EvalContext ctx) {
        return new NumberValue(Math.exp(Evaluator.toNumber(args.get(0))));
    }

    static CellValue pi(List<CellValue> args, EvalContext ctx) {
        return new NumberValue(Math.PI);
    }

    static CellValue rand(List<CellValue> args, EvalContext ctx) {
        return new NumberValue(Math.random());
    }

    static CellValue randBetween(List<CellValue> args, EvalContext ctx) {
        int low = (int) Evaluator.toNumber(args.get(0));
        int high = (int) Evaluator.toNumber(args.get(1));
        return new NumberValue(low + (int)(Math.random() * (high - low + 1)));
    }

    static CellValue fact(List<CellValue> args, EvalContext ctx) {
        int n = (int) Evaluator.toNumber(args.get(0));
        if (n < 0) return new ErrorValue(ErrorType.NUM);
        double result = 1;
        for (int i = 2; i <= n; i++) result *= i;
        return new NumberValue(result);
    }

    static CellValue sumProduct(List<CellValue> args, EvalContext ctx) {
        double sum = 0;
        for (CellValue v : args) sum += Evaluator.toNumber(v);
        return new NumberValue(sum);
    }

    static CellValue sumSq(List<CellValue> args, EvalContext ctx) {
        double sum = 0;
        for (CellValue v : args) {
            double n = Evaluator.toNumber(v);
            sum += n * n;
        }
        return new NumberValue(sum);
    }
}
