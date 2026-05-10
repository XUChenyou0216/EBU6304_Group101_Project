package com.ta.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Compares job application deadlines with the current date (system default time zone).
 * A deadline is considered "past" once the calendar day after the deadline date starts
 * (i.e. still active on the deadline day, closed from the next day onward).
 */
public final class JobDeadlineUtil {

    private JobDeadlineUtil() {}

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * @return parsed date, or null if the string cannot be parsed
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
     * @return true when today is strictly after the deadline date
     */
    public static boolean isPastDeadline(String deadline) {
        LocalDate d = parseDeadline(deadline);
        if (d == null) return false;
        return LocalDate.now().isAfter(d);
    }
}
