package io.excel4j.core.io;

import io.excel4j.core.io.internal.DateConverter;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.*;

class DateConverterTest {

    @Test
    void localDateToSerial() {
        double serial = DateConverter.toSerial(LocalDate.of(2024, 1, 15));
        assertThat(serial).isGreaterThan(0);
    }

    @Test
    void serialToLocalDateRoundTrip() {
        LocalDate original = LocalDate.of(2023, 6, 20);
        double serial = DateConverter.toSerial(original);
        LocalDate recovered = DateConverter.toLocalDate(serial);
        assertThat(recovered).isEqualTo(original);
    }

    @Test
    void modernDateRoundTrip() {
        LocalDate original = LocalDate.of(2024, 12, 31);
        double serial = DateConverter.toSerial(original);
        LocalDate recovered = DateConverter.toLocalDate(serial);
        assertThat(recovered).isEqualTo(original);
    }

    @Test
    void handle1900LeapYearBug() {
        // Excel serial 61 = 1900-03-01 (serial 60 is fake "1900-02-29")
        LocalDate date = DateConverter.toLocalDate(61.0);
        assertThat(date).isEqualTo(LocalDate.of(1900, 3, 1));
    }

    @Test
    void serialWithTimeFractionRoundTrip() {
        LocalDateTime original = LocalDateTime.of(2024, 6, 15, 14, 30, 0);
        double serial = DateConverter.toSerial(original);
        LocalDateTime recovered = DateConverter.toLocalDateTime(serial);
        assertThat(recovered.toLocalDate()).isEqualTo(original.toLocalDate());
        assertThat(recovered.getHour()).isEqualTo(14);
        assertThat(recovered.getMinute()).isEqualTo(30);
    }
}
