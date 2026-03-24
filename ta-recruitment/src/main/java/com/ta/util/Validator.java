package com.ta.util;

import java.util.regex.Pattern;

public class Validator {
    private static final Pattern EMAIL_RE = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PHONE_RE = Pattern.compile("^[0-9]{6,15}$");

    public static String requireNonEmpty(String value, String field) {
        if (value == null || value.trim().isEmpty()) return field + " is required.";
        return null;
    }

    public static String validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) return "Email is required.";
        if (!EMAIL_RE.matcher(email.trim()).matches()) return "Invalid email format.";
        return null;
    }

    public static String validatePassword(String pwd) {
        if (pwd == null || pwd.length() < 6) return "Password must be at least 6 characters.";
        return null;
    }

    public static String validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) return null;
        if (!PHONE_RE.matcher(phone.trim()).matches()) return "Phone must be 6-15 digits.";
        return null;
    }

    public static String validateCvFile(String fileName, long fileSize) {
        if (fileName == null || fileName.isEmpty()) return "Please select a file.";
        String lower = fileName.toLowerCase();
        if (!lower.endsWith(".pdf") && !lower.endsWith(".docx") && !lower.endsWith(".doc"))
            return "Invalid file format. Supported: PDF, DOC, DOCX.";
        if (fileSize > 10 * 1024 * 1024) return "File exceeds 10MB limit.";
        return null;
    }

    public static String validatePositiveInt(String value, String field) {
        if (value == null || value.trim().isEmpty()) return field + " is required.";
        try {
            if (Integer.parseInt(value.trim()) <= 0) return field + " must be positive.";
        } catch (NumberFormatException e) { return field + " must be a valid number."; }
        return null;
    }

    public static String validateDate(String date, String field) {
        if (date == null || date.trim().isEmpty()) return field + " is required.";
        if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) return field + " format: yyyy-MM-dd.";
        return null;
    }
}
