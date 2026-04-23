package com.ta.dao;

import com.ta.model.Application;
import com.ta.model.Job;
import com.ta.util.FileManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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

    public Application findById(String applicationId) {
        for (Application a : findAll()) {
            if (a.getApplicationId().equals(applicationId)) return a;
        }
        return null;
    }

    public List<Application> findByTa(String taUserId) {
        return findAll().stream().filter(a -> a.getTaUserId().equals(taUserId)).collect(Collectors.toList());
    }

    public List<Application> findByJob(String jobId) {
        return findAll().stream()
                .filter(a -> Objects.equals(a.getJobId(), jobId))
                .collect(Collectors.toList());
    }

    /** Number of applications already marked {@code ACCEPTED} for this job. */
    public long countAcceptedForJob(String jobId) {
        if (jobId == null) {
            return 0;
        }
        return findByJob(jobId).stream().filter(a -> "ACCEPTED".equals(a.getStatus())).count();
    }

    /**
     * Whether setting {@code newStatusUpper} to ACCEPTED would exceed {@link Job#getVacancies()}.
     * Does not block: non-ACCEPTED targets, or rows already ACCEPTED (e.g. editing review note only).
     */
    public boolean isAcceptanceCapExceeded(Job job, Application app, String newStatusUpper) {
        if (job == null || app == null || newStatusUpper == null) {
            return false;
        }
        if (!"ACCEPTED".equals(newStatusUpper)) {
            return false;
        }
        if ("ACCEPTED".equals(app.getStatus())) {
            return false;
        }
        int cap = job.getVacancies();
        if (cap < 0) {
            cap = 0;
        }
        return countAcceptedForJob(app.getJobId()) >= cap;
    }

    /**
     * All applications for the given job ids, newest {@link Application#getAppliedDate()} first
     * (ISO dates sort lexicographically). Ties broken by applicationId descending.
     */
    public List<Application> findByJobIdsSortedByAppliedDateDesc(Set<String> jobIds) {
        if (jobIds == null || jobIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<Application> list = findAll().stream()
                .filter(a -> a.getJobId() != null && jobIds.contains(a.getJobId()))
                .collect(Collectors.toList());
        list.sort((a, b) -> {
            String da = a.getAppliedDate() != null ? a.getAppliedDate() : "";
            String db = b.getAppliedDate() != null ? b.getAppliedDate() : "";
            int c = db.compareTo(da);
            if (c != 0) {
                return c;
            }
            String ida = a.getApplicationId() != null ? a.getApplicationId() : "";
            String idb = b.getApplicationId() != null ? b.getApplicationId() : "";
            return idb.compareTo(ida);
        });
        return list;
    }

    public boolean hasApplied(String taUserId, String jobId) {
        return findAll().stream()
                .anyMatch(a -> Objects.equals(a.getTaUserId(), taUserId) && Objects.equals(a.getJobId(), jobId));
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
