package com.ta.util;

/**
 * Utility for determining MIME content types and file characteristics of uploaded CV documents.
 * <p>
 * Supports the file extensions accepted by {@link Validator#validateCvFile(String, long)}:
 * PDF ({@code .pdf}), legacy Word ({@code .doc}), and Office Open XML Word ({@code .docx}).
 * </p>
 */
public final class CvMimeUtil {
    private CvMimeUtil() {}

    /**
     * Guesses the HTTP content type for a CV file based on its file name extension.
     *
     * @param fileName the original file name including extension (may be {@code null})
     * @return the MIME type string; {@code "application/octet-stream"} for unknown or
     *         unrecognized extensions
     */
    public static String guessContentType(String fileName) {
        if (fileName == null) return "application/octet-stream";
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".docx"))
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (lower.endsWith(".doc")) return "application/msword";
        return "application/octet-stream";
    }

    /**
     * Determines whether a file name refers to a PDF document.
     *
     * @param fileName the file name to inspect (may be {@code null})
     * @return {@code true} if {@code fileName} ends with {@code .pdf} (case-insensitive)
     */
    public static boolean isPdf(String fileName) {
        return fileName != null && fileName.toLowerCase().endsWith(".pdf");
    }
}
