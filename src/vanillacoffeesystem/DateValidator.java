package vanillacoffeesystem;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public final class DateValidator {

    private static final LocalDate MIN_DATE = LocalDate.of(2020, 1, 1);
    private static final DateTimeFormatter STRICT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    private DateValidator() {
    }

    public static String validatePaymentDate(LocalDate date) {
        if (date == null) {
            return "Please select a payment date.";
        }
        if (date.isBefore(MIN_DATE)) {
            return "Payment date cannot be before " + MIN_DATE + ".";
        }
        if (date.isAfter(LocalDate.now())) {
            return "Payment date cannot be in the future.";
        }
        return null;
    }

    public static String validateDateText(String text) {
        if (text == null || text.isBlank()) {
            return "Please enter a date (YYYY-MM-DD).";
        }
        try {
            LocalDate parsed = LocalDate.parse(text.trim(), STRICT);
            return validatePaymentDate(parsed);
        } catch (DateTimeParseException ex) {
            return "Invalid date. Use a real calendar date (YYYY-MM-DD), e.g. 2026-06-17.";
        }
    }

    public static LocalDate getMinDate() {
        return MIN_DATE;
    }

    public static LocalDate getMaxDate() {
        return LocalDate.now();
    }
}
