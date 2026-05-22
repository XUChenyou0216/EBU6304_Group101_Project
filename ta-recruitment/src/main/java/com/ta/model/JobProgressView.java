package com.ta.model;

/**
 * Immutable view model for the Module Organiser recruitment progress table.
 * Aggregates per-job applicant counts, fill rate, and display metadata so JSP
 * pages can render progress rows without performing calculations in the view layer.
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

    /**
     * Creates a progress view row with pre-computed recruitment metrics.
     *
     * @param jobTitle         short title of the job posting
     * @param moduleName       human-readable module name
     * @param vacancies        total number of TA positions available
     * @param applicantsCount  total number of applications received
     * @param underReviewCount number of applications currently under review
     * @param acceptedCount    number of applications accepted so far
     * @param fillRate         percentage of vacancies filled (0–100)
     * @param statusClass      CSS class suffix for status badge styling
     * @param statusLabel      human-readable status label for display
     */
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

    /** @return the short job title */
    public String getJobTitle() {
        return jobTitle;
    }

    /** @return the human-readable module name */
    public String getModuleName() {
        return moduleName;
    }

    /** @return the total number of TA vacancies for this job */
    public int getVacancies() {
        return vacancies;
    }

    /** @return the total number of applications received */
    public int getApplicantsCount() {
        return applicantsCount;
    }

    /** @return the number of applications currently under review */
    public int getUnderReviewCount() {
        return underReviewCount;
    }

    /** @return the number of applications accepted so far */
    public int getAcceptedCount() {
        return acceptedCount;
    }

    /** @return the fill rate as a percentage (0–100) */
    public int getFillRate() {
        return fillRate;
    }

    /** @return the CSS class suffix used for status badge styling */
    public String getStatusClass() {
        return statusClass;
    }

    /** @return the human-readable status label for display */
    public String getStatusLabel() {
        return statusLabel;
    }
}
