package com.ta.model;

public class Notification {

    // ── Notification type constants ──────────────────────────────────────
    /** TA submitted an application (recipient: MO). */
    public static final String TYPE_APPLICATION_SUBMITTED = "APPLICATION_SUBMITTED";
    /** Application status changed (recipient: TA). */
    public static final String TYPE_STATUS_UPDATED        = "STATUS_UPDATED";
    /** MO posted a new job successfully (recipient: MO). */
    public static final String TYPE_JOB_POSTED            = "JOB_POSTED";
    /** A TA's estimated workload exceeds the limit (recipient: ADMIN). */
    public static final String TYPE_WORKLOAD_ALERT        = "WORKLOAD_ALERT";

    private String notificationId;
    private String userId;
    private String type;
    private String message;
    private boolean isRead;
    private String createdDate;

    public Notification() {}

    public Notification(String notificationId, String userId, String type,
                        String message, boolean isRead, String createdDate) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.type = type;
        this.message = message;
        this.isRead = isRead;
        this.createdDate = createdDate;
    }

    public String getNotificationId() { return notificationId; }
    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public String getCreatedDate() { return createdDate; }
    public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }

    public String toCsvRow() {
        return String.join(",",
            notificationId, userId, type,
            "\"" + (message != null ? message.replace("\"", "\"\"") : "") + "\"",
            String.valueOf(isRead),
            createdDate);
    }

    public static final String CSV_HEADER =
        "notificationId,userId,type,message,isRead,createdDate";

    /**
     * Returns a Bootstrap color context string based on the notification type.
     * <ul>
     *   <li>{@code JOB_POSTED}      → "success"  (green)</li>
     *   <li>{@code WORKLOAD_ALERT}  → "danger"   (red)</li>
     *   <li>{@code STATUS_UPDATED}  → "info"     (cyan)</li>
     *   <li>{@code APPLICATION_SUBMITTED} → "primary" (blue)</li>
     *   <li>others                  → "secondary" (grey)</li>
     * </ul>
     */
    public String getColor() {
        if (type == null) return "secondary";
        switch (type) {
            case TYPE_JOB_POSTED:            return "success";
            case TYPE_WORKLOAD_ALERT:        return "danger";
            case TYPE_STATUS_UPDATED:        return "info";
            case TYPE_APPLICATION_SUBMITTED: return "primary";
            default:                         return "secondary";
        }
    }

    public static Notification fromCsvRow(String row) {
        String[] f = Job.parseCsv(row);
        if (f.length < 6) return null;
        boolean read = "true".equalsIgnoreCase(f[4]);
        return new Notification(f[0], f[1], f[2], f[3], read, f[5]);
    }
}
