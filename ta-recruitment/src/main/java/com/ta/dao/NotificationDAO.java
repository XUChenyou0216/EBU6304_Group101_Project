package com.ta.dao;

import com.ta.model.Notification;
import com.ta.util.FileManager;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Data access object for user {@link Notification} records in {@code notifications.csv}.
 * <p>
 * Supports listing notifications per user, counting unread items, creating new
 * notifications, and marking individual or all notifications as read.
 * </p>
 */
public class NotificationDAO {
    private final String filePath;

    /**
     * Creates a DAO that reads and writes notifications from {@code dataDir/notifications.csv}.
     *
     * @param dataDir base directory containing CSV data files
     */
    public NotificationDAO(String dataDir) {
        this.filePath = dataDir + "/notifications.csv";
    }

    /**
     * Loads every valid notification row from the backing CSV file.
     *
     * @return list of {@link Notification} instances; never {@code null} (may be empty)
     */
    public List<Notification> findAll() {
        List<Notification> list = new ArrayList<>();
        for (String row : FileManager.readAll(filePath)) {
            Notification n = Notification.fromCsvRow(row);
            if (n != null) list.add(n);
        }
        return list;
    }

    /**
     * Returns all notifications addressed to the specified user.
     *
     * @param userId the recipient's user id
     * @return notifications where {@link Notification#getUserId()} equals {@code userId}; never {@code null}
     */
    public List<Notification> findByUser(String userId) {
        return findAll().stream()
            .filter(n -> n.getUserId().equals(userId))
            .collect(Collectors.toList());
    }

    /**
     * Counts how many unread notifications belong to the given user.
     *
     * @param userId the recipient's user id
     * @return number of notifications for that user with {@link Notification#isRead()} {@code false}
     */
    public int countUnread(String userId) {
        return (int) findAll().stream()
            .filter(n -> n.getUserId().equals(userId) && !n.isRead())
            .count();
    }

    /**
     * Appends a new notification row to the CSV file.
     *
     * @param n the notification to persist; must not be {@code null}
     */
    public void save(Notification n) {
        FileManager.appendRow(filePath, Notification.CSV_HEADER, n.toCsvRow());
    }

    /**
     * Marks a single notification as read by id and rewrites the backing file.
     *
     * @param notificationId the notification id to update
     */
    public void markRead(String notificationId) {
        List<String> rows = FileManager.readAll(filePath);
        List<String> updated = new ArrayList<>();
        for (String row : rows) {
            Notification n = Notification.fromCsvRow(row);
            if (n != null && n.getNotificationId().equals(notificationId)) {
                n.setRead(true);
                updated.add(n.toCsvRow());
            } else {
                updated.add(row);
            }
        }
        FileManager.writeAll(filePath, Notification.CSV_HEADER, updated);
    }

    /**
     * Marks every unread notification for the given user as read and persists the file.
     *
     * @param userId the recipient whose notifications should be marked read
     */
    public void markAllRead(String userId) {
        List<String> rows = FileManager.readAll(filePath);
        List<String> updated = new ArrayList<>();
        for (String row : rows) {
            Notification n = Notification.fromCsvRow(row);
            if (n != null && n.getUserId().equals(userId) && !n.isRead()) {
                n.setRead(true);
                updated.add(n.toCsvRow());
            } else {
                updated.add(row);
            }
        }
        FileManager.writeAll(filePath, Notification.CSV_HEADER, updated);
    }

    /**
     * Computes the next available notification id with prefix {@code NTF} without persisting.
     *
     * @return the next id string (for example {@code NTF001})
     */
    public String generateNextId() {
        return FileManager.generateNextId(filePath, "NTF");
    }
}
