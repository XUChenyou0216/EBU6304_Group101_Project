package com.ta.dao;

import com.ta.model.Application;
import com.ta.model.Job;
import com.ta.util.FileManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Data access object for TA {@link Application} records in {@code applications.csv}.
 * <p>
 * Supports querying by applicant, job, and status; enforces vacancy caps when
 * accepting applications; and provides bulk persistence for batch updates.
 * </p>
 */
public class ApplicationDAO {
    private final String filePath;

    /**
     * Creates a DAO that reads and writes applications from {@code dataDir/applications.csv}.
     *
     * @param dataDir base directory containing CSV data files
     */
    public ApplicationDAO(String dataDir) { this.filePath = dataDir + "/applications.csv"; }

    /**
     * Loads every valid application row from the backing CSV file.
     *
     * @return list of {@link Application} instances; never {@code null} (may be empty)
     */
    public List<Application> findAll() {
        List<Application> apps = new ArrayList<>();
        for (String row : FileManager.readAll(filePath)) {
            Application a = Application.fromCsvRow(row);
            if (a != null) apps.add(a);
        }
        return apps;
    }

    /**
     * Finds a single application by its unique application id.
     *
     * @param applicationId the application identifier
     * @return the matching {@link Application}, or {@code null} if not found
     */
    public Application findById(String applicationId) {
        for (Application a : findAll()) {
            if (a.getApplicationId().equals(applicationId)) return a;
        }
        return null;
    }

    /**
     * Returns all applications submitted by the given teaching assistant.
     *
     * @param taUserId the TA's user id
     * @return applications where {@link Application#getTaUserId()} equals {@code taUserId}; never {@code null}
     */
    public List<Application> findByTa(String taUserId) {
        return findAll().stream().filter(a -> a.getTaUserId().equals(taUserId)).collect(Collectors.toList());
    }

    /**
     * Returns all applications for a specific job posting.
     *
     * @param jobId the job id to filter on (may be {@code null}, in which case no rows match)
     * @return applications for that job; never {@code null}
     */
    public List<Application> findByJob(String jobId) {
        return findAll().stream()
                .filter(a -> Objects.equals(a.getJobId(), jobId))
                .collect(Collectors.toList());
    }

    /**
     * Counts how many applications for the given job already have status {@code ACCEPTED}.
     *
     * @param jobId the job id; if {@code null}, returns {@code 0}
     * @return number of accepted applications for that job
     */
    public long countAcceptedForJob(String jobId) {
        if (jobId == null) {
            return 0;
        }
        return findByJob(jobId).stream().filter(a -> "ACCEPTED".equals(a.getStatus())).count();
    }

    /**
     * Determines whether changing an application's status to {@code ACCEPTED} would exceed
     * the job's vacancy limit defined by {@link Job#getVacancies()}.
     * <p>
     * Returns {@code false} when: {@code job}, {@code app}, or {@code newStatusUpper} is
     * {@code null}; the target status is not {@code ACCEPTED}; or the application is already
     * {@code ACCEPTED} (for example when only updating a review note).
     * </p>
     *
     * @param job            the job posting with vacancy count
     * @param app            the application being updated
     * @param newStatusUpper proposed status in upper case (for example {@code ACCEPTED})
     * @return {@code true} if accepting would meet or exceed the cap; {@code false} otherwise
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
     * Returns applications whose job id is in the given set, sorted by applied date
     * descending (newest first).
     * <p>
     * ISO date strings sort lexicographically. When applied dates tie, application ids
     * are compared in descending order.
     * </p>
     *
     * @param jobIds set of job ids to include; {@code null} or empty yields an empty list
     * @return sorted list of matching applications; never {@code null}
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

    /**
     * Checks whether the given TA has already applied to the specified job.
     *
     * @param taUserId the teaching assistant's user id
     * @param jobId    the job id
     * @return {@code true} if at least one application exists for that TA–job pair
     */
    public boolean hasApplied(String taUserId, String jobId) {
        return findAll().stream()
                .anyMatch(a -> Objects.equals(a.getTaUserId(), taUserId) && Objects.equals(a.getJobId(), jobId));
    }

    /**
     * Appends an application row; the caller must set {@link Application#getApplicationId()}
     * before calling.
     *
     * @param app the application to persist
     */
    public void save(Application app) {
        FileManager.appendRow(filePath, Application.CSV_HEADER, app.toCsvRow());
    }


    /**
     * Appends an application and assigns the next id with prefix {@code APP}.
     *
     * @param app the application to save; updated in memory with the new id on success
     * @return the generated application id, or {@code null} if append failed
     */
    public String saveWithNextId(Application app) {
        String generatedId = FileManager.appendWithGeneratedId(filePath, Application.CSV_HEADER, "APP", id -> {
            app.setApplicationId(id);
            return app.toCsvRow();
        });
        if (generatedId != null) {
            app.setApplicationId(generatedId);
        }
        return generatedId;
    }

    /**
     * Appends an application only if the TA has not already applied to the same job.
     * <p>
     * On success, assigns the next id with prefix {@code APP} via {@link FileManager#nextId}.
     * </p>
     *
     * @param app the application to insert
     * @return {@code true} if written; {@code false} if a duplicate TA–job application exists
     */

    public boolean saveIfNotApplied(Application app) {
        return FileManager.appendIfAbsent(
                filePath,
                Application.CSV_HEADER,
                row -> {
                    Application existing = Application.fromCsvRow(row);
                    return existing != null
                            && existing.getTaUserId().equals(app.getTaUserId())
                            && existing.getJobId().equals(app.getJobId());
                },
                rows -> {
                    String id = FileManager.nextId(rows, "APP");
                    app.setApplicationId(id);
                    return app.toCsvRow();
                }
        );
    }


    /**
     * Replaces the CSV row whose application id matches {@link Application#getApplicationId()}
     * on the given object.
     *
     * @param updated application with new field values
     */

    public void update(Application updated) {
        FileManager.updateRows(filePath, Application.CSV_HEADER, rows -> {
            List<String> newRows = new ArrayList<>();
            for (String row : rows) {
                Application a = Application.fromCsvRow(row);
                if (a != null && a.getApplicationId().equals(updated.getApplicationId()))
                    newRows.add(updated.toCsvRow());
                else newRows.add(row);
            }
            return newRows;
        });
    }

    /**
     * Replaces the entire applications CSV in one write, preserving the order of the
     * supplied list.
     *
     * @param applications complete list of applications to write; must not be {@code null}
     */
    public void persistAll(List<Application> applications) {
        List<String> rows = new ArrayList<>(applications.size());
        for (Application a : applications) {
            rows.add(a.toCsvRow());
        }
        FileManager.writeAll(filePath, Application.CSV_HEADER, rows);
    }

    /**
     * Computes the next available application id with prefix {@code APP} without persisting.
     *
     * @return the next id string (for example {@code APP001})
     */
    public String generateNextId() {
        return FileManager.generateNextId(filePath, "APP");
    }
}
