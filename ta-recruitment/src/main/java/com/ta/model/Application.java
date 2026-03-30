package com.ta.model;

/**
 * CSV-backed application row used by {@link com.ta.dao.ApplicationDAO} and servlets.
 * JPA entity (database mapping): {@link com.university.tarecruitment.entity.Application}.
 */
public class Application {
    private String applicationId;
    private String taUserId;
    private String jobId;
    private String status;       // "SUBMITTED", "UNDER_REVIEW", "ACCEPTED", "REJECTED"
    private String appliedDate;
    private String reviewNote;

    public Application() {}

    public Application(String applicationId, String taUserId, String jobId,
                       String status, String appliedDate, String reviewNote) {
        this.applicationId = applicationId;
        this.taUserId = taUserId;
        this.jobId = jobId;
        this.status = status;
        this.appliedDate = appliedDate;
        this.reviewNote = reviewNote;
    }

    public String getApplicationId() { return applicationId; }
    public void setApplicationId(String id) { this.applicationId = id; }
    public String getTaUserId() { return taUserId; }
    public void setTaUserId(String id) { this.taUserId = id; }
    public String getJobId() { return jobId; }
    public void setJobId(String id) { this.jobId = id; }
    public String getStatus() { return status; }
    public void setStatus(String s) { this.status = s; }
    public String getAppliedDate() { return appliedDate; }
    public void setAppliedDate(String d) { this.appliedDate = d; }
    public String getReviewNote() { return reviewNote; }
    public void setReviewNote(String n) { this.reviewNote = n; }

    public String toCsvRow() {
        return String.join(",", applicationId, taUserId, jobId, status, appliedDate,
            reviewNote != null ? "\"" + reviewNote.replace("\"","\"\"") + "\"" : "");
    }

    public static final String CSV_HEADER =
        "applicationId,taUserId,jobId,status,appliedDate,reviewNote";

    public static Application fromCsvRow(String row) {
        String[] f = Job.parseCsv(row);
        if (f.length < 6) return null;
        return new Application(f[0], f[1], f[2], f[3], f[4], f[5]);
    }
}
