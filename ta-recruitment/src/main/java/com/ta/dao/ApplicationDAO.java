package com.ta.dao;

import com.ta.model.Application;
import com.ta.util.FileManager;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ApplicationDAO {
    private final String filePath;

    public ApplicationDAO(String dataDir) { this.filePath = dataDir + "/applications.csv"; }

    public List<Application> findAll() {
        List<Application> apps = new ArrayList<>();
        for (String row : FileManager.readAll(filePath)) {
            Application a = Application.fromCsvRow(row);
            if (a != null) apps.add(a);
        }
        return apps;
    }

    public List<Application> findByTa(String taUserId) {
        return findAll().stream().filter(a -> a.getTaUserId().equals(taUserId)).collect(Collectors.toList());
    }

    public List<Application> findByJob(String jobId) {
        return findAll().stream().filter(a -> a.getJobId().equals(jobId)).collect(Collectors.toList());
    }

    public Application findById(String applicationId) {
        for (Application a : findAll()) {
            if (a.getApplicationId().equals(applicationId)) return a;
        }
        return null;
    }

    public boolean hasApplied(String taUserId, String jobId) {
        return findAll().stream().anyMatch(a -> a.getTaUserId().equals(taUserId) && a.getJobId().equals(jobId));
    }

    public void save(Application app) {
        FileManager.appendRow(filePath, Application.CSV_HEADER, app.toCsvRow());
    }

    public void update(Application updated) {
        List<String> rows = FileManager.readAll(filePath);
        List<String> newRows = new ArrayList<>();
        for (String row : rows) {
            Application a = Application.fromCsvRow(row);
            if (a != null && a.getApplicationId().equals(updated.getApplicationId()))
                newRows.add(updated.toCsvRow());
            else newRows.add(row);
        }
        FileManager.writeAll(filePath, Application.CSV_HEADER, newRows);
    }

    public String generateNextId() {
        return FileManager.generateNextId(filePath, "APP");
    }
}
