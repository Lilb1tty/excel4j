package io.github.lilb1tty.cellix.core.io.internal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public final class DateConverter {

    private static final LocalDate EXCEL_EPOCH = LocalDate.of(1899, 12, 31);
    private static final int LEAP_BUG_SERIAL = 60;

    private DateConverter() {}

    public static LocalDate toLocalDate(double serial) {
        long days = (long) serial;
        if (days > LEAP_BUG_SERIAL) days--;
        return EXCEL_EPOCH.plusDays(days);
    }

    public static LocalDateTime toLocalDateTime(double serial) {
        LocalDate date = toLocalDate(serial);
        double fraction = serial - Math.floor(serial);
        long totalSeconds = Math.round(fraction * 86400);
        LocalTime time = LocalTime.ofSecondOfDay(totalSeconds % 86400);
        return LocalDateTime.of(date, time);
    }

    public static double toSerial(LocalDate date) {
        long days = EXCEL_EPOCH.until(date, ChronoUnit.DAYS);
        if (days >= LEAP_BUG_SERIAL) days++;
        return (double) days;
    }

    public static double toSerial(LocalDateTime dateTime) {
        double datePart = toSerial(dateTime.toLocalDate());
        double timeFraction = (dateTime.getHour() * 3600L
            + dateTime.getMinute() * 60L
            + dateTime.getSecond()) / 86400.0;
        return datePart + timeFraction;
    }
}
