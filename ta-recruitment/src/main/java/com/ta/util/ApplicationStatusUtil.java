package com.ta.util;

/**
 * Application status labels and grouping for MO Applicants UI (spec 2024/25).
 */
public final class ApplicationStatusUtil {

    private ApplicationStatusUtil() {}

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

    /** CSS suffix for badge: st-pending, st-under_review, ... */
    public static String badgeKey(String status) {
        if (status == null) return "pending";
        return status.toLowerCase();
    }

    public static boolean isPending(String s) {
        if (s == null) return true;
        String u = s.toUpperCase();
        return "SUBMITTED".equals(u) || "PENDING".equals(u);
    }

    public static boolean isReviewing(String s) {
        if (s == null) return false;
        String u = s.toUpperCase();
        return "UNDER_REVIEW".equals(u) || "INTERVIEWED".equals(u);
    }

    public static boolean isProcessed(String s) {
        if (s == null) return false;
        String u = s.toUpperCase();
        return "ACCEPTED".equals(u) || "REJECTED".equals(u) || "OFFERED".equals(u) || "WITHDRAWN".equals(u);
    }
}
