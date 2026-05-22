package com.ta.util;

/**
 * Utility for mapping internal application status codes to user-facing labels and UI groupings.
 * <p>
 * Status values follow the MO Applicants UI specification (2024/25). Internal codes use
 * uppercase underscore-separated tokens (e.g. {@code UNDER_REVIEW}); display labels use
 * title-case English phrases suitable for badges and tables.
 * </p>
 */
public final class ApplicationStatusUtil {

    private ApplicationStatusUtil() {}

    /**
     * Converts an internal status code to a human-readable display label.
     * <p>
     * Known codes are mapped to fixed labels; unknown codes have underscores replaced
     * with spaces. A {@code null} status is treated as {@code "Pending"}.
     * </p>
     *
     * @param status the internal status code (e.g. {@code "SUBMITTED"}, {@code "OFFERED"})
     * @return the display label for the status (never {@code null})
     */
    public static String displayLabel(String status) {
        if (status == null) return "Pending";
        switch (status.toUpperCase()) {
            case "SUBMITTED":
            case "PENDING":
                return "Pending";
            case "UNDER_REVIEW":
                return "Under Review";
            case "INTERVIEWED":
                return "Interviewed";
            case "OFFERED":
                return "Offered";
            case "ACCEPTED":
                return "Accepted";
            case "REJECTED":
                return "Rejected";
            case "WITHDRAWN":
                return "Withdrawn";
            default:
                return status.replace("_", " ");
        }
    }

    /**
     * Returns a lowercase CSS class suffix for styling status badges.
     * <p>
     * Example: {@code "UNDER_REVIEW"} yields {@code "under_review"}, producing a class
     * such as {@code st-under_review}.
     * </p>
     *
     * @param status the internal status code
     * @return the lowercase badge key; {@code "pending"} when {@code status} is {@code null}
     */
    public static String badgeKey(String status) {
        if (status == null) return "pending";
        return status.toLowerCase();
    }

    /**
     * Determines whether an application is in a pending (not yet reviewed) state.
     *
     * @param s the internal status code (may be {@code null})
     * @return {@code true} if {@code s} is {@code null}, {@code "SUBMITTED"}, or {@code "PENDING"}
     */
    public static boolean isPending(String s) {
        if (s == null) return true;
        String u = s.toUpperCase();
        return "SUBMITTED".equals(u) || "PENDING".equals(u);
    }

    /**
     * Determines whether an application is actively under review or has been interviewed.
     *
     * @param s the internal status code (may be {@code null})
     * @return {@code true} if {@code s} is {@code "UNDER_REVIEW"} or {@code "INTERVIEWED"}
     */
    public static boolean isReviewing(String s) {
        if (s == null) return false;
        String u = s.toUpperCase();
        return "UNDER_REVIEW".equals(u) || "INTERVIEWED".equals(u);
    }

    /**
     * Determines whether an application has reached a terminal or decision state.
     *
     * @param s the internal status code (may be {@code null})
     * @return {@code true} if {@code s} is {@code "ACCEPTED"}, {@code "REJECTED"},
     *         {@code "OFFERED"}, or {@code "WITHDRAWN"}
     */
    public static boolean isProcessed(String s) {
        if (s == null) return false;
        String u = s.toUpperCase();
        return "ACCEPTED".equals(u) || "REJECTED".equals(u) || "OFFERED".equals(u) || "WITHDRAWN".equals(u);
    }
}
