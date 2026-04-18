package com.ta.dao;

import com.ta.model.Notification;
import com.ta.util.FileManager;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NotificationDAO {
    private final String filePath;

    public NotificationDAO(String dataDir) {
        this.filePath = dataDir + "/notifications.csv";
    }

    public List<Notification> findAll() {
        List<Notification> list = new ArrayList<>();
        for (String row : FileManager.readAll(filePath)) {
            Notification n = Notification.fromCsvRow(row);
            if (n != null) list.add(n);
        }
        return list;
    }

    public List<Notification> findByUser(String userId) {
        return findAll().stream()
            .filter(n -> n.getUserId().equals(userId))
            .collect(Collectors.toList());
    }

    public int countUnread(String userId) {
        return (int) findAll().stream()
            .filter(n -> n.getUserId().equals(userId) && !n.isRead())
            .count();
    }

    public void save(Notification n) {
        FileManager.appendRow(filePath, Notification.CSV_HEADER, n.toCsvRow());
    }

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

    public String generateNextId() {
        return FileManager.generateNextId(filePath, "NTF");
    }
}
