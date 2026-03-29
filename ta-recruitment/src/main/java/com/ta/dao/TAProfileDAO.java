package com.ta.dao;

import com.ta.model.TAProfile;
import com.ta.util.FileManager;
import java.util.ArrayList;
import java.util.List;

public class TAProfileDAO {
    private final String filePath;

    public TAProfileDAO(String dataDir) { this.filePath = dataDir + "/profiles.csv"; }

    public TAProfile findByUserId(String userId) {
        for (String row : FileManager.readAll(filePath)) {
            TAProfile p = TAProfile.fromCsvRow(row);
            if (p != null && p.getUserId().equals(userId)) return p;
        }
        return null;
    }

    public List<TAProfile> findAll() {
        List<TAProfile> profiles = new ArrayList<>();
        for (String row : FileManager.readAll(filePath)) {
            TAProfile p = TAProfile.fromCsvRow(row);
            if (p != null) profiles.add(p);
        }
        return profiles;
    }

    public void save(TAProfile profile) {
        FileManager.appendRow(filePath, TAProfile.CSV_HEADER, profile.toCsvRow());
    }

    public void saveOrUpdate(TAProfile profile) {
        if (findByUserId(profile.getUserId()) != null) {
            List<String> rows = FileManager.readAll(filePath);
            List<String> newRows = new ArrayList<>();
            for (String row : rows) {
                TAProfile p = TAProfile.fromCsvRow(row);
                if (p != null && p.getUserId().equals(profile.getUserId()))
                    newRows.add(profile.toCsvRow());
                else newRows.add(row);
            }
            FileManager.writeAll(filePath, TAProfile.CSV_HEADER, newRows);
        } else { save(profile); }
    }
}
