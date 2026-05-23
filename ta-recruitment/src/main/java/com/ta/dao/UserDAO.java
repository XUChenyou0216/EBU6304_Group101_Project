package com.ta.dao;

import com.ta.model.User;
import com.ta.util.FileManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access object for {@link User} records persisted in a CSV file.
 * <p>
 * Each instance is bound to a single {@code users.csv} file under the configured
 * data directory. All read and write operations are delegated to {@link FileManager}.
 * </p>
 */
public class UserDAO {
    private final String filePath;

    /**
     * Creates a DAO that reads and writes users from {@code dataDir/users.csv}.
     *
     * @param dataDir base directory containing CSV data files (must not be {@code null})
     */
    public UserDAO(String dataDir) { this.filePath = dataDir + "/users.csv"; }

    /**
     * Loads every valid user row from the backing CSV file.
     *
     * @return list of parsed {@link User} instances; never {@code null} (may be empty)
     */
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        for (String row : FileManager.readAll(filePath)) {
            User u = User.fromCsvRow(row);
            if (u != null) users.add(u);
        }
        return users;
    }

    /**
     * Looks up a user by username, ignoring case.
     *
     * @param username the login name to search for
     * @return the matching {@link User}, or {@code null} if none exists
     */
    public User findByUsername(String username) {
        for (User u : findAll())
            if (u.getUsername().equalsIgnoreCase(username)) return u;
        return null;
    }

    /**
     * Looks up a user by unique identifier.
     *
     * @param userId the user id to match (case-sensitive)
     * @return the matching {@link User}, or {@code null} if none exists
     */
    public User findById(String userId) {
        for (User u : findAll())
            if (u.getUserId().equals(userId)) return u;
        return null;
    }

    /**
     * Appends a new user row to the CSV file without checking for duplicate usernames.
     * <p>
     * The caller is responsible for setting {@link User#getUserId()} before calling
     * this method, or the row may be written with a missing or placeholder id.
     * </p>
     *
     * @param user the user to persist; must not be {@code null}
     */
    public void save(User user) {
        FileManager.appendRow(filePath, User.CSV_HEADER, user.toCsvRow());
    }


    /**
     * Appends a new user only if no existing row has the same username (case-insensitive).
     * <p>
     * On success, assigns the next generated id with prefix {@code U} via
     * {@link FileManager#nextId} and writes the row.
     * </p>
     *
     * @param user the user to insert; username must be unique
     * @return {@code true} if the row was written; {@code false} if a duplicate username exists
     */

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


    /**
     * Replaces the CSV row whose user id matches {@link User#getUserId()} on the
     * given object; all other rows are left unchanged.
     *
     * @param updated the user record with the new field values
     */

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


    /**
     * Removes the user with the specified id from the backing file.
     * <p>
     * Rows that fail to parse or belong to other users are retained.
     * </p>
     *
     * @param userId the id of the user to delete
     */

    public void delete(String userId) {
        List<String> rows = FileManager.readAll(filePath);
        List<String> newRows = new ArrayList<>();
        for (String row : rows) {
            User u = User.fromCsvRow(row);
            if (u != null && !u.getUserId().equals(userId)) {
                newRows.add(row);
            }
        }
        FileManager.writeAll(filePath, User.CSV_HEADER, newRows);
    }


    /**
     * Computes the next available user id using prefix {@code U} without persisting a row.
     *
     * @return the next id string (for example {@code U001})
     */
    public String generateNextId() {
        return FileManager.generateNextId(filePath, "U");
    }
}
