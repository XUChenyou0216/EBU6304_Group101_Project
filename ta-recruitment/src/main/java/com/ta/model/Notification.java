package com.ta.model;

public class Notification {
    private String notificationId;
    private String userId;
    private String type;        // "APPLICATION_SUBMITTED", "STATUS_UPDATED"
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

    public static Notification fromCsvRow(String row) {
        String[] f = Job.parseCsv(row);
        if (f.length < 6) return null;
        boolean read = "true".equalsIgnoreCase(f[4]);
        return new Notification(f[0], f[1], f[2], f[3], read, f[5]);
    }
}
