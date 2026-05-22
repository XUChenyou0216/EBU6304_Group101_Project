package com.ta.model;

/**
 * Domain model representing a Teaching Assistant's application to a job posting.
 * Tracks submission status through the review workflow ({@code SUBMITTED},
 * {@code UNDER_REVIEW}, {@code ACCEPTED}, {@code REJECTED}) and optional
 * reviewer notes. Instances are persisted as CSV rows.
 */
public class Application {
    private String applicationId;
    private String taUserId;
    private String jobId;
    private String status;       // "SUBMITTED", "UNDER_REVIEW", "ACCEPTED", "REJECTED"
    private String appliedDate;
    private String reviewNote;

    /** Creates an empty application with all fields unset. */
    public Application() {}

    /**
     * Creates an application with all fields populated.
     *
     * @param applicationId unique identifier for this application
     * @param taUserId      user id of the applying Teaching Assistant
     * @param jobId         id of the job being applied to
     * @param status        workflow status ({@code SUBMITTED}, {@code UNDER_REVIEW},
     *                      {@code ACCEPTED}, or {@code REJECTED})
     * @param appliedDate   date the application was submitted (ISO date string)
     * @param reviewNote    optional note from the reviewer (may be {@code null})
     */
    public Application(String applicationId, String taUserId, String jobId,
                       String status, String appliedDate, String reviewNote) {
        this.applicationId = applicationId;
        this.taUserId = taUserId;
        this.jobId = jobId;
        this.status = status;
        this.appliedDate = appliedDate;
        this.reviewNote = reviewNote;
    }

    /** @return the unique application identifier */
    public String getApplicationId() { return applicationId; }

    /** @param id the unique application identifier to set */
    public void setApplicationId(String id) { this.applicationId = id; }

    /** @return the user id of the applying Teaching Assistant */
    public String getTaUserId() { return taUserId; }

    /** @param id the Teaching Assistant user id to set */
    public void setTaUserId(String id) { this.taUserId = id; }

    /** @return the id of the job this application targets */
    public String getJobId() { return jobId; }

    /** @param id the target job id to set */
    public void setJobId(String id) { this.jobId = id; }

    /** @return the workflow status of this application */
    public String getStatus() { return status; }

    /** @param s the workflow status to set */
    public void setStatus(String s) { this.status = s; }

    /** @return the date this application was submitted (ISO date string) */
    public String getAppliedDate() { return appliedDate; }

    /** @param d the applied date to set */
    public void setAppliedDate(String d) { this.appliedDate = d; }

    /** @return the optional reviewer note, or {@code null} if none */
    public String getReviewNote() { return reviewNote; }

    /** @param n the reviewer note to set */
    public void setReviewNote(String n) { this.reviewNote = n; }

    /**
     * Serialises this application to a single CSV row (no header).
     * The review note field is quoted when present to preserve embedded commas.
     *
     * @return comma-separated field values matching {@link #CSV_HEADER}
     */
    public String toCsvRow() {
        return String.join(",", applicationId, taUserId, jobId, status, appliedDate,
            reviewNote != null ? "\"" + reviewNote.replace("\"","\"\"") + "\"" : "");
    }

    /** CSV column header for application persistence files. */
    public static final String CSV_HEADER =
        "applicationId,taUserId,jobId,status,appliedDate,reviewNote";

    /**
     * Parses a CSV row into an {@link Application} instance.
     * Uses {@link Job#parseCsv(String)} to handle quoted fields.
     *
     * @param row a single CSV line (without header)
     * @return a populated application, or {@code null} if the row has fewer than six fields
     */
    public static Application fromCsvRow(String row) {
        String[] f = Job.parseCsv(row);
        if (f.length < 6) return null;
        return new Application(f[0], f[1], f[2], f[3], f[4], f[5]);
    }
}
