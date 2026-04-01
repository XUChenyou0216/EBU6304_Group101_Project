package com.ta.util;

public final class CvMimeUtil {
    private CvMimeUtil() {}

    public static String guessContentType(String fileName) {
        if (fileName == null) return "application/octet-stream";
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".docx"))
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (lower.endsWith(".doc")) return "application/msword";
        return "application/octet-stream";
    }

    public static boolean isPdf(String fileName) {
        return fileName != null && fileName.toLowerCase().endsWith(".pdf");
    }
}
