package com.university.tarecruitment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatCardDTO {

    private String id;
    private String title;
    private String icon;
    private String iconBackground;
    private Integer value;
    private Integer previousValue;
    private String change;
    private String changeType; // POSITIVE, NEGATIVE, NEUTRAL, WARNING
    private String description;
    private Integer maxValue; // For progress bar
    private Double progressPercentage;
    private Boolean isWarning;

    public String getChangeColor() {
        if (changeType == null) {
            return "#475569";
        }
        switch (changeType) {
            case "POSITIVE":
                return "#16A34A";
            case "NEGATIVE":
                return "#DC2626";
            case "WARNING":
                return "#EA580C";
            case "NEUTRAL":
            default:
                return "#475569";
        }
    }

    public String getChangeIcon() {
        if (changeType == null) {
            return "➡️";
        }
        switch (changeType) {
            case "POSITIVE":
                return "📈";
            case "NEGATIVE":
                return "📉";
            case "WARNING":
                return "⚠️";
            case "NEUTRAL":
            default:
                return "➡️";
        }
    }

    public String getValueColor() {
        if (Boolean.TRUE.equals(isWarning)) {
            return "#EA580C";
        }
        if (changeType == null) {
            return "#0F172A";
        }
        switch (changeType) {
            case "POSITIVE":
                return "#16A34A";
            case "NEGATIVE":
                return "#DC2626";
            case "WARNING":
                return "#EA580C";
            default:
                return "#0F172A";
        }
    }
}
