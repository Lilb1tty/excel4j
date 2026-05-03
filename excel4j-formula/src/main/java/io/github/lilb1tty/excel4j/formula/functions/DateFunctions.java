package io.github.lilb1tty.excel4j.formula.functions;

import io.github.lilb1tty.excel4j.core.model.*;
import io.github.lilb1tty.excel4j.core.io.internal.DateConverter;
import io.github.lilb1tty.excel4j.formula.EvalContext;
import io.github.lilb1tty.excel4j.formula.Evaluator;
import io.github.lilb1tty.excel4j.formula.FunctionRegistry;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;
import java.util.List;

public final class DateFunctions {

    private DateFunctions() {}

    public static void registerAll(FunctionRegistry reg) {
        reg.register("DATE", DateFunctions::date);
        reg.register("TODAY", DateFunctions::today);
        reg.register("NOW", DateFunctions::now);
        reg.register("YEAR", DateFunctions::year);
        reg.register("MONTH", DateFunctions::month);
        reg.register("DAY", DateFunctions::day);
        reg.register("HOUR", DateFunctions::hour);
        reg.register("MINUTE", DateFunctions::minute);
        reg.register("SECOND", DateFunctions::second);
        reg.register("WEEKDAY", DateFunctions::weekday);
        reg.register("EDATE", DateFunctions::edate);
        reg.register("EOMONTH", DateFunctions::eomonth);
        reg.register("DAYS", DateFunctions::days);
        reg.register("TIME", DateFunctions::time);
        reg.register("DATEDIF", DateFunctions::datedif);
        reg.register("ISOWEEKNUM", DateFunctions::isoWeekNum);
    }

    static CellValue date(List<CellValue> args, EvalContext ctx) {
        int year = (int) Evaluator.toNumber(args.get(0));
        int month = (int) Evaluator.toNumber(args.get(1));
        int day = (int) Evaluator.toNumber(args.get(2));
        LocalDate d = LocalDate.of(year, 1, 1).plusMonths(month - 1).plusDays(day - 1);
        return new DateValue(d);
    }

    static CellValue today(List<CellValue> args, EvalContext ctx) {
        return new DateValue(LocalDate.now());
    }

    static CellValue now(List<CellValue> args, EvalContext ctx) {
        return new DateTimeValue(LocalDateTime.now());
    }

    static CellValue year(List<CellValue> args, EvalContext ctx) {
        CellValue v = args.get(0);
        LocalDate d = toLocalDate(v);
        if (d == null) return new ErrorValue(ErrorType.VALUE);
        return new NumberValue(d.getYear());
    }

    static CellValue month(List<CellValue> args, EvalContext ctx) {
        LocalDate d = toLocalDate(args.get(0));
        if (d == null) return new ErrorValue(ErrorType.VALUE);
        return new NumberValue(d.getMonthValue());
    }

    static CellValue day(List<CellValue> args, EvalContext ctx) {
        LocalDate d = toLocalDate(args.get(0));
        if (d == null) return new ErrorValue(ErrorType.VALUE);
        return new NumberValue(d.getDayOfMonth());
    }

    static CellValue hour(List<CellValue> args, EvalContext ctx) {
        LocalDateTime dt = toLocalDateTime(args.get(0));
        if (dt == null) return new ErrorValue(ErrorType.VALUE);
        return new NumberValue(dt.getHour());
    }

    static CellValue minute(List<CellValue> args, EvalContext ctx) {
        LocalDateTime dt = toLocalDateTime(args.get(0));
        if (dt == null) return new ErrorValue(ErrorType.VALUE);
        return new NumberValue(dt.getMinute());
    }

    static CellValue second(List<CellValue> args, EvalContext ctx) {
        LocalDateTime dt = toLocalDateTime(args.get(0));
        if (dt == null) return new ErrorValue(ErrorType.VALUE);
        return new NumberValue(dt.getSecond());
    }

    static CellValue weekday(List<CellValue> args, EvalContext ctx) {
        LocalDate d = toLocalDate(args.get(0));
        if (d == null) return new ErrorValue(ErrorType.VALUE);
        int dow = d.getDayOfWeek().getValue(); // 1=Mon ... 7=Sun
        int type = args.size() > 1 ? (int) Evaluator.toNumber(args.get(1)) : 1;
        return switch (type) {
            case 1 -> new NumberValue(dow == 7 ? 1 : dow + 1); // Sun=1...Sat=7
            case 2 -> new NumberValue(dow); // Mon=1...Sun=7
            case 3 -> new NumberValue(dow == 7 ? 0 : dow); // Mon=1...Sun=0
            default -> new ErrorValue(ErrorType.NUM);
        };
    }

    static LocalDate toLocalDate(CellValue v) {
        return switch (v) {
            case DateValue(var d) -> d;
            case DateTimeValue(var dt) -> dt.toLocalDate();
            case NumberValue(var n) -> DateConverter.toLocalDate(n);
            default -> null;
        };
    }

    static LocalDateTime toLocalDateTime(CellValue v) {
        return switch (v) {
            case DateTimeValue(var dt) -> dt;
            case DateValue(var d) -> d.atStartOfDay();
            case NumberValue(var n) -> DateConverter.toLocalDateTime(n);
            default -> null;
        };
    }

    static CellValue edate(List<CellValue> args, EvalContext ctx) {
        LocalDate d = toLocalDate(args.get(0));
        if (d == null) return new ErrorValue(ErrorType.VALUE);
        int months = (int) Evaluator.toNumber(args.get(1));
        return new DateValue(d.plusMonths(months));
    }

    static CellValue eomonth(List<CellValue> args, EvalContext ctx) {
        LocalDate d = toLocalDate(args.get(0));
        if (d == null) return new ErrorValue(ErrorType.VALUE);
        int months = (int) Evaluator.toNumber(args.get(1));
        LocalDate target = d.plusMonths(months);
        return new DateValue(target.withDayOfMonth(target.lengthOfMonth()));
    }

    static CellValue days(List<CellValue> args, EvalContext ctx) {
        LocalDate end = toLocalDate(args.get(0));
        LocalDate start = toLocalDate(args.get(1));
        if (end == null || start == null) return new ErrorValue(ErrorType.VALUE);
        return new NumberValue(start.until(end, ChronoUnit.DAYS));
    }

    static CellValue time(List<CellValue> args, EvalContext ctx) {
        int hour = (int) Evaluator.toNumber(args.get(0));
        int min = (int) Evaluator.toNumber(args.get(1));
        int sec = (int) Evaluator.toNumber(args.get(2));
        return new DateTimeValue(
            LocalDate.of(1899, 12, 31).atTime(LocalTime.of(hour % 24, min % 60, sec % 60))
        );
    }

    static CellValue datedif(List<CellValue> args, EvalContext ctx) {
        LocalDate start = toLocalDate(args.get(0));
        LocalDate end = toLocalDate(args.get(1));
        String unit = Evaluator.toText(args.get(2)).toUpperCase();
        if (start == null || end == null) return new ErrorValue(ErrorType.VALUE);
        return switch (unit) {
            case "Y" -> new NumberValue(start.until(end, ChronoUnit.YEARS));
            case "M" -> new NumberValue(start.until(end, ChronoUnit.MONTHS));
            case "D" -> new NumberValue(start.until(end, ChronoUnit.DAYS));
            case "MD" -> new NumberValue(
                start.withYear(end.getYear()).withMonth(end.getMonthValue()).until(end, ChronoUnit.DAYS)
            );
            case "YM" -> new NumberValue((end.getMonthValue() - start.getMonthValue() + 12) % 12);
            case "YD" -> new NumberValue(start.withYear(end.getYear()).until(end, ChronoUnit.DAYS));
            default -> new ErrorValue(ErrorType.VALUE);
        };
    }

    static CellValue isoWeekNum(List<CellValue> args, EvalContext ctx) {
        LocalDate d = toLocalDate(args.get(0));
        if (d == null) return new ErrorValue(ErrorType.VALUE);
        return new NumberValue(d.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR));
    }
}
