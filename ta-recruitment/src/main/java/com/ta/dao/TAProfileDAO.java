package com.ta.dao;

import com.ta.model.TAProfile;
import com.ta.util.FileManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access object for teaching assistant {@link TAProfile} records in {@code profiles.csv}.
 * <p>
 * Each profile is keyed by user id. The DAO supports lookup, listing, append-only save,
 * and upsert via {@link #saveOrUpdate(TAProfile)}.
 * </p>
 */
public class TAProfileDAO {
    private final String filePath;

    /**
     * Creates a DAO that reads and writes TA profiles from {@code dataDir/profiles.csv}.
     *
     * @param dataDir base directory containing CSV data files
     */
    public TAProfileDAO(String dataDir) { this.filePath = dataDir + "/profiles.csv"; }

    /**
     * Finds the profile associated with a single user id.
     *
     * @param userId the TA user's id
     * @return the matching {@link TAProfile}, or {@code null} if none exists
     */
    public TAProfile findByUserId(String userId) {
        for (String row : FileManager.readAll(filePath)) {
            TAProfile p = TAProfile.fromCsvRow(row);
            if (p != null && p.getUserId().equals(userId)) return p;
        }
        return null;
    }

    /**
     * Loads every valid TA profile row from the backing CSV file.
     *
     * @return list of {@link TAProfile} instances; never {@code null} (may be empty)
     */
    public List<TAProfile> findAll() {
        List<TAProfile> profiles = new ArrayList<>();
        for (String row : FileManager.readAll(filePath)) {
            TAProfile p = TAProfile.fromCsvRow(row);
            if (p != null) profiles.add(p);
        }
        return profiles;
    }

    /**
     * Appends a new profile row without checking for an existing user id.
     * <p>
     * Prefer {@link #saveOrUpdate(TAProfile)} when the profile may already exist.
     * </p>
     *
     * @param profile the profile to append
     */
    public void save(TAProfile profile) {
        FileManager.appendRow(filePath, TAProfile.CSV_HEADER, profile.toCsvRow());
    }

    /**
     * Updates the row for {@link TAProfile#getUserId()} if present; otherwise appends a new row.
     *
     * @param profile the profile data to write or replace
     */
    public void saveOrUpdate(TAProfile profile) {
        FileManager.updateRows(filePath, TAProfile.CSV_HEADER, rows -> {
            List<String> newRows = new ArrayList<>();
            boolean updated = false;
            for (String row : rows) {
                TAProfile p = TAProfile.fromCsvRow(row);
                if (p != null && p.getUserId().equals(profile.getUserId())) {
                    newRows.add(profile.toCsvRow());
                    updated = true;
                } else {
                    newRows.add(row);
                }
            }
            if (!updated) {
                newRows.add(profile.toCsvRow());
            }
            return newRows;
        });
    }
}
