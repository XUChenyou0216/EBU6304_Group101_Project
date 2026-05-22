package com.ta.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utility for parsing and comparing job application deadline dates against the current date.
 * <p>
 * Comparisons use the system default time zone. A deadline is considered active on the
 * deadline calendar day itself and becomes past starting from the following day
 * (i.e. {@link #isPastDeadline(String)} returns {@code true} only when today is strictly
 * after the parsed deadline date).
 * </p>
 */
public final class JobDeadlineUtil {

    private JobDeadlineUtil() {}

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * Parses a deadline string into a {@link LocalDate}.
     * <p>
     * Accepts ISO-8601 format ({@code yyyy-MM-dd}) first; if that fails, falls back to
     * a flexible pattern ({@code yyyy-M-d}) allowing single-digit month and day components.
     * </p>
     *
     * @param deadline the deadline date string (may be {@code null} or blank)
     * @return the parsed {@link LocalDate}, or {@code null} if the input is missing or
     *         cannot be parsed
     */
    public static LocalDate parseDeadline(String deadline) {
        if (deadline == null) return null;
        String s = deadline.trim();
        if (s.isEmpty()) return null;
        try {
            return LocalDate.parse(s, ISO);
        } catch (DateTimeParseException e) {
            try {
                return LocalDate.parse(s, DateTimeFormatter.ofPattern("yyyy-M-d"));
            } catch (DateTimeParseException e2) {
                return null;
            }
        }
    }

    /**
     * Determines whether the application deadline has passed relative to today's date.
     * <p>
     * The deadline day itself is still considered open; the deadline is past only when
     * today's date is strictly after the parsed deadline date.
     * </p>
     *
     * @param deadline the deadline date string to evaluate
     * @return {@code true} if today is strictly after the parsed deadline date;
     *         {@code false} if the deadline is today or in the future, or if parsing fails
     */
    public static boolean isPastDeadline(String deadline) {
        LocalDate d = parseDeadline(deadline);
        if (d == null) return false;
        return LocalDate.now().isAfter(d);
    }
}
