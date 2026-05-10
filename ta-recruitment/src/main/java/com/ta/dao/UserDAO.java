package com.ta.dao;

import com.ta.model.User;
import com.ta.util.FileManager;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private final String filePath;

    public UserDAO(String dataDir) { this.filePath = dataDir + "/users.csv"; }

    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        for (String row : FileManager.readAll(filePath)) {
            User u = User.fromCsvRow(row);
            if (u != null) users.add(u);
        }
        return users;
    }

    public User findByUsername(String username) {
        for (User u : findAll())
            if (u.getUsername().equalsIgnoreCase(username)) return u;
        return null;
    }

    public User findById(String userId) {
        for (User u : findAll())
            if (u.getUserId().equals(userId)) return u;
        return null;
    }

    public void save(User user) {
        FileManager.appendRow(filePath, User.CSV_HEADER, user.toCsvRow());
    }

    public boolean saveIfUsernameAvailable(User user) {
        return FileManager.appendIfAbsent(
                filePath,
                User.CSV_HEADER,
                row -> {
                    User existing = User.fromCsvRow(row);
                    return existing != null && existing.getUsername().equalsIgnoreCase(user.getUsername());
                },
                rows -> {
                    String id = FileManager.nextId(rows, "U");
                    user.setUserId(id);
                    return user.toCsvRow();
                }
        );
    }

    public void update(User updated) {
        FileManager.updateRows(filePath, User.CSV_HEADER, rows -> {
            List<String> newRows = new ArrayList<>();
            for (String row : rows) {
                User u = User.fromCsvRow(row);
                if (u != null && u.getUserId().equals(updated.getUserId()))
                    newRows.add(updated.toCsvRow());
                else newRows.add(row);
            }
            return newRows;
        });
    }

    public String generateNextId() {
        return FileManager.generateNextId(filePath, "U");
    }
}
