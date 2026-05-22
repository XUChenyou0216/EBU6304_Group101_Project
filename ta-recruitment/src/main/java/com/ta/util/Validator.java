package com.ta.util;

import java.util.regex.Pattern;

/**
 * Centralized input validation for the TA Recruitment application.
 * <p>
 * Each validation method returns {@code null} when the input is valid, or a human-readable
 * English error message describing the first validation failure encountered.
 * </p>
 */
public class Validator {
    private static final Pattern EMAIL_RE = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PHONE_RE = Pattern.compile("^[0-9]{6,15}$");

    /**
     * Validates that a string field is non-null and not blank after trimming.
     *
     * @param value the value to check
     * @param field the display name of the field, used in the error message
     * @return {@code null} if valid, or {@code "{field} is required."} otherwise
     */
    public static String requireNonEmpty(String value, String field) {
        if (value == null || value.trim().isEmpty()) return field + " is required.";
        return null;
    }

    /**
     * Validates all required fields on a student profile form.
     *
     * @param studentId    the student's identifier
     * @param fullName     the student's full name
     * @param programme    the academic programme name
     * @param yearOfStudy  the year of study (e.g. {@code "2"})
     * @return {@code null} if all fields are valid, or the first error message encountered
     */
    public static String validateProfile(String studentId, String fullName, String programme, String yearOfStudy) {
        String err;
        if ((err = requireNonEmpty(studentId, "Student ID")) != null) return err;
        if ((err = requireNonEmpty(fullName, "Full Name")) != null) return err;
        if ((err = requireNonEmpty(programme, "Programme")) != null) return err;
        if ((err = requireNonEmpty(yearOfStudy, "Year of Study")) != null) return err;
        return null;
    }


    /**
     * Validates that a password meets minimum strength requirements.
     * <p>
     * Requirements: at least 6 characters, one uppercase letter, one lowercase letter,
     * and one digit.
     * </p>
     *
     * @param pwd the password to validate
     * @return {@code null} if valid, or a descriptive error message otherwise
     */

    public static String validateStrongPassword(String pwd) {
        if (pwd == null || pwd.length() < 6) return "Password must be at least 6 characters.";
        if (!pwd.matches(".*[A-Z].*")) return "Password must contain at least one uppercase letter.";
        if (!pwd.matches(".*[a-z].*")) return "Password must contain at least one lowercase letter.";
        if (!pwd.matches(".*[0-9].*")) return "Password must contain at least one digit.";
        return null;
    }


    /**
     * Validates an email address for presence and basic format.
     *
     * @param email the email address to validate
     * @return {@code null} if valid, or an error message if missing or malformed
     */

    public static String validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) return "Email is required.";
        if (!EMAIL_RE.matcher(email.trim()).matches()) return "Invalid email format.";
        return null;
    }

    /**
     * Validates a password using the same rules as {@link #validateStrongPassword(String)}.
     *
     * @param pwd the password to validate
     * @return {@code null} if valid, or a descriptive error message otherwise
     * @see #validateStrongPassword(String)
     */
    public static String validatePassword(String pwd) {
        return validateStrongPassword(pwd);
    }

    /**
     * Validates an optional phone number.
     * <p>
     * An empty or null phone number is considered valid (field is optional).
     * When provided, the value must contain 6–15 digits with no other characters.
     * </p>
     *
     * @param phone the phone number to validate (may be {@code null} or blank)
     * @return {@code null} if valid or omitted, or an error message if the format is invalid
     */
    public static String validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) return null;
        if (!PHONE_RE.matcher(phone.trim()).matches()) return "Phone must be 6-15 digits.";
        return null;
    }

    /**
     * Validates an uploaded CV file by name and size.
     * <p>
     * Accepted extensions: {@code .pdf}, {@code .doc}, {@code .docx}.
     * Maximum file size: 10 MB.
     * </p>
     *
     * @param fileName the original file name including extension
     * @param fileSize the file size in bytes
     * @return {@code null} if valid, or an error message describing the violation
     */
    public static String validateCvFile(String fileName, long fileSize) {
        if (fileName == null || fileName.isEmpty()) return "Please select a file.";
        String lower = fileName.toLowerCase();
        if (!lower.endsWith(".pdf") && !lower.endsWith(".docx") && !lower.endsWith(".doc"))
            return "Invalid file format. Supported: PDF, DOC, DOCX.";
        if (fileSize > 10 * 1024 * 1024) return "File exceeds 10MB limit.";
        return null;
    }

    /**
     * Validates that a string represents a positive integer.
     *
     * @param value the string value to parse and validate
     * @param field the display name of the field, used in error messages
     * @return {@code null} if valid, or an error message if empty, non-numeric, or not positive
     */
    public static String validatePositiveInt(String value, String field) {
        if (value == null || value.trim().isEmpty()) return field + " is required.";
        try {
            if (Integer.parseInt(value.trim()) <= 0) return field + " must be positive.";
        } catch (NumberFormatException e) { return field + " must be a valid number."; }
        return null;
    }

    /**
     * Validates that a date string is present and matches the {@code yyyy-MM-dd} format.
     *
     * @param date  the date string to validate
     * @param field the display name of the field, used in error messages
     * @return {@code null} if valid, or an error message if missing or incorrectly formatted
     */
    public static String validateDate(String date, String field) {
        if (date == null || date.trim().isEmpty()) return field + " is required.";
        if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) return field + " format: yyyy-MM-dd.";
        return null;
    }

    /**
     * Validates all required fields for a job posting form.
     * <p>
     * Ensures the module name and deadline are non-empty, and that vacancies is a
     * positive integer (not negative, zero, or non-numeric).
     * </p>
     *
     * @param moduleName the module or course name for the TA position
     * @param vacancies  the number of open positions as a string
     * @param deadline   the application deadline date string
     * @return {@code null} if all fields are valid, or the first error message encountered
     */
    public static String validateJob(String moduleName, String vacancies, String deadline) {
        // 校验模块名称（必填）
        String err = requireNonEmpty(moduleName, "Module Name");
        if (err != null) return err;

        // 校验招聘名额（必须是正整数）
        err = validatePositiveInt(vacancies, "Vacancies");
        if (err != null) return err;

        // 校验截止日期（必填）
        err = requireNonEmpty(deadline, "Deadline");
        return err;
    }


    /**
     * Sanitizes a string for safe inclusion in a CSV field by replacing line breaks with spaces.
     *
     * @param input the raw input string (may be {@code null})
     * @return the sanitized string, or an empty string if {@code input} is {@code null}
     */

    public static String sanitizeForCsv(String input) {
        if (input == null) return "";
        return input.replace("\r\n", " ").replace("\n", " ").replace("\r", " ");
    }
}
