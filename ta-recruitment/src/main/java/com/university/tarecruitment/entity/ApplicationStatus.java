package com.university.tarecruitment.entity;

import lombok.Getter;

@Getter
public enum ApplicationStatus {
    PENDING("Pending", "#F1F5F9", "#64748B", "#CBD5E1"),
    UNDER_REVIEW("Under Review", "#DBEAFE", "#2563EB", "#93C5FD"),
    INTERVIEWED("Interviewed", "#E9D5FF", "#9333EA", "#C084FC"),
    OFFERED("Offered", "#D1FAE5", "#059669", "#6EE7B7"),
    ACCEPTED("Accepted", "#DCFCE7", "#16A34A", "#86EFAC"),
    REJECTED("Rejected", "#FEE2E2", "#DC2626", "#FECACA"),
    WITHDRAWN("Withdrawn", "#FED7AA", "#EA580C", "#FDBA74");

    private final String displayName;
    private final String backgroundColor;
    private final String textColor;
    private final String borderColor;

    ApplicationStatus(String displayName, String backgroundColor, String textColor, String borderColor) {
        this.displayName = displayName;
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
        this.borderColor = borderColor;
    }

    public static ApplicationStatus fromString(String status) {
        for (ApplicationStatus s : ApplicationStatus.values()) {
            if (s.name().equalsIgnoreCase(status)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + status);
    }
}
