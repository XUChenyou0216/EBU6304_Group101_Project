package com.ta.model;

/**
 * Domain model representing an in-app notification delivered to a user.
 * Notifications are typed ({@link #TYPE_APPLICATION_SUBMITTED}, etc.) and
 * may be marked read or unread. Used to alert Module Organisers, Teaching
 * Assistants, and administrators of recruitment events. Instances are persisted
 * as CSV rows.
 */
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

    /** Creates an empty notification with all fields unset. */
    public Notification() {}

    /**
     * Creates a notification with all fields populated.
     *
     * @param notificationId unique identifier for this notification
     * @param userId         id of the recipient user
     * @param type           notification type (one of the {@code TYPE_*} constants)
     * @param message        human-readable notification body
     * @param isRead         whether the recipient has read this notification
     * @param createdDate    date the notification was created (ISO date string)
     */
    public Notification(String notificationId, String userId, String type,
                        String message, boolean isRead, String createdDate) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.type = type;
        this.message = message;
        this.isRead = isRead;
        this.createdDate = createdDate;
    }

    /** @return the unique notification identifier */
    public String getNotificationId() { return notificationId; }

    /** @param notificationId the unique notification identifier to set */
    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }

    /** @return the id of the recipient user */
    public String getUserId() { return userId; }

    /** @param userId the recipient user id to set */
    public void setUserId(String userId) { this.userId = userId; }

    /** @return the notification type (one of the {@code TYPE_*} constants) */
    public String getType() { return type; }

    /** @param type the notification type to set */
    public void setType(String type) { this.type = type; }

    /** @return the human-readable notification message */
    public String getMessage() { return message; }

    /** @param message the notification message to set */
    public void setMessage(String message) { this.message = message; }

    /** @return {@code true} if the recipient has read this notification */
    public boolean isRead() { return isRead; }

    /** @param read {@code true} to mark as read, {@code false} to mark as unread */
    public void setRead(boolean read) { isRead = read; }

    /** @return the date this notification was created (ISO date string) */
    public String getCreatedDate() { return createdDate; }

    /** @param createdDate the creation date to set */
    public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }

    /**
     * Serialises this notification to a single CSV row (no header).
     * The message field is always quoted to preserve embedded commas.
     *
     * @return comma-separated field values matching {@link #CSV_HEADER}
     */
    public String toCsvRow() {
        return String.join(",",
            notificationId, userId, type,
            "\"" + (message != null ? message.replace("\"", "\"\"") : "") + "\"",
            String.valueOf(isRead),
            createdDate);
    }

    /** CSV column header for notification persistence files. */
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

     *
     * @return Bootstrap contextual colour name for UI rendering

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


    /**
     * Parses a CSV row into a {@link Notification} instance.
     * Uses {@link Job#parseCsv(String)} to handle quoted message fields.
     *
     * @param row a single CSV line (without header)
     * @return a populated notification, or {@code null} if the row has fewer than six fields
     */

    public static Notification fromCsvRow(String row) {
        String[] f = Job.parseCsv(row);
        if (f.length < 6) return null;
        boolean read = "true".equalsIgnoreCase(f[4]);
        return new Notification(f[0], f[1], f[2], f[3], read, f[5]);
    }
}
