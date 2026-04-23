package com.ta.model;

/**
 * View model for the MO recruitment progress table.
 */
public class JobProgressView {
    private final String jobTitle;
    private final String moduleName;
    private final int vacancies;
    private final int applicantsCount;
    private final int underReviewCount;
    private final int acceptedCount;
    private final int fillRate;
    private final String statusClass;
    private final String statusLabel;

    public JobProgressView(String jobTitle, String moduleName, int vacancies,
                           int applicantsCount, int underReviewCount, int acceptedCount,
                           int fillRate, String statusClass, String statusLabel) {
        this.jobTitle = jobTitle;
        this.moduleName = moduleName;
        this.vacancies = vacancies;
        this.applicantsCount = applicantsCount;
        this.underReviewCount = underReviewCount;
        this.acceptedCount = acceptedCount;
        this.fillRate = fillRate;
        this.statusClass = statusClass;
        this.statusLabel = statusLabel;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public String getModuleName() {
        return moduleName;
    }

    public int getVacancies() {
        return vacancies;
    }

    public int getApplicantsCount() {
        return applicantsCount;
    }

    public int getUnderReviewCount() {
        return underReviewCount;
    }

    public int getAcceptedCount() {
        return acceptedCount;
    }

    public int getFillRate() {
        return fillRate;
    }

    public String getStatusClass() {
        return statusClass;
    }

    public String getStatusLabel() {
        return statusLabel;
    }
}
