package io.github.lilb1tty.cellix.report;

import io.github.lilb1tty.cellix.core.model.*;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

public final class ValueResolver {

    private ValueResolver() {}

    public static CellValue resolve(ReportContext ctx, String expression) {
        Object value;

        if (expression.contains(".")) {
            String[] parts = expression.split("\\.");
            value = ctx.get(parts[0]);
            for (int i = 1; i < parts.length && value != null; i++) {
                value = resolveProperty(value, parts[i]);
            }
        } else {
            if (ctx.getItem() != null) {
                value = resolveProperty(ctx.getItem(), expression);
            } else {
                value = ctx.get(expression);
            }
        }

        return toCellValue(value);
    }

    private static Object resolveProperty(Object obj, String property) {
        if (obj instanceof Map<?, ?> map) {
            return map.get(property);
        }
        String methodName = "get" + property.substring(0, 1).toUpperCase() + property.substring(1);
        try {
            Method method = obj.getClass().getMethod(methodName);
            return method.invoke(obj);
        } catch (Exception e) {
            try {
                var field = obj.getClass().getDeclaredField(property);
                field.setAccessible(true);
                return field.get(obj);
            } catch (Exception ex) {
                return null;
            }
        }
    }

    private static CellValue toCellValue(Object value) {
        if (value == null) {
            return new BlankValue();
        }
        return switch (value) {
            case String s -> new TextValue(s);
            case Number n -> new NumberValue(n.doubleValue());
            case Boolean b -> new BooleanValue(b);
            case LocalDate d -> new DateValue(d);
            case LocalDateTime d -> new DateTimeValue(d);
            default -> new TextValue(value.toString());
        };
    }
}
