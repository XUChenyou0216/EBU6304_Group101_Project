package com.ta.dao;

import com.ta.model.Job;
import com.ta.util.FileManager;
import com.ta.util.JobDeadlineUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Data access object for {@link Job} postings stored in {@code jobs.csv}.
 * <p>
 * {@link #findAll()} may automatically close jobs whose deadline has passed by
 * updating status from {@code OPEN} to {@code CLOSED} and persisting the change.
 * </p>
 */
public class JobDAO {
    private final String filePath;

    /**
     * Creates a DAO that reads and writes jobs from {@code dataDir/jobs.csv}.
     *
     * @param dataDir base directory containing CSV data files
     */
    public JobDAO(String dataDir) { this.filePath = dataDir + "/jobs.csv"; }

    /**
     * Returns all jobs from the backing file, auto-closing any {@code OPEN} job
     * whose deadline is in the past according to {@link JobDeadlineUtil}.
     * <p>
     * If any status was updated, the full job list is written back to disk before
     * the result is returned.
     * </p>
     *
     * @return list of {@link Job} instances; never {@code null} (may be empty)
     */
    public List<Job> findAll() {
        List<Job> jobs = readAllFromFile();
        boolean changed = false;
        for (Job j : jobs) {
            if ("OPEN".equals(j.getStatus()) && JobDeadlineUtil.isPastDeadline(j.getDeadline())) {
                j.setStatus("CLOSED");
                changed = true;
            }
        }
        if (changed) {
            persistAll(jobs);
        }
        return jobs;
    }

    private List<Job> readAllFromFile() {
        List<Job> jobs = new ArrayList<>();
        for (String row : FileManager.readAll(filePath)) {
            Job j = Job.fromCsvRow(row);
            if (j != null) jobs.add(j);
        }
        return jobs;
    }

    private void persistAll(List<Job> jobs) {
        List<String> rows = new ArrayList<>();
        for (Job j : jobs) {
            rows.add(j.toCsvRow());
        }
        FileManager.writeAll(filePath, Job.CSV_HEADER, rows);
    }

    /**
     * Replaces the entire jobs CSV with the given list in one write operation.
     * <p>
     * Typically used after batch status updates or when the caller already holds
     * the authoritative in-memory job list.
     * </p>
     *
     * @param jobs complete list of jobs to persist; must not be {@code null}
     */
    public void writeAllJobs(List<Job> jobs) {
        persistAll(jobs);
    }

    /**
     * Finds a single job by its unique job id.
     * <p>
     * Uses {@link #findAll()}, so deadline-based auto-closing may run as a side effect.
     * </p>
     *
     * @param jobId the job identifier to match
     * @return the matching {@link Job}, or {@code null} if not found
     */
    public Job findById(String jobId) {
        for (Job j : findAll()) {
            if (Objects.equals(j.getJobId(), jobId)) {
                return j;
            }
        }
        return null;
    }

    /**
     * Returns all jobs whose status is {@code OPEN}.
     *
     * @return filtered list of open jobs; never {@code null}
     */
    public List<Job> findOpen() {
        return findAll().stream().filter(j -> "OPEN".equals(j.getStatus())).collect(Collectors.toList());
    }

    /**
     * Returns all jobs created by the specified module organizer.
     *
     * @param moUserId the module organizer's user id
     * @return jobs whose {@link Job#getMoUserId()} equals {@code moUserId}; never {@code null}
     */
    public List<Job> findByMo(String moUserId) {
        return findAll().stream().filter(j -> j.getMoUserId().equals(moUserId)).collect(Collectors.toList());
    }

    /**
     * Appends a new job row without assigning an id; the caller must set
     * {@link Job#getJobId()} beforehand.
     *
     * @param job the job record to append
     */
    public void save(Job job) {
        FileManager.appendRow(filePath, Job.CSV_HEADER, job.toCsvRow());
    }

    /**
     * Appends a job row and assigns the next generated id with prefix {@code J}.
     * <p>
     * The in-memory {@code job} object is updated with the new id when generation succeeds.
     * </p>
     *
     * @param job the job to save; fields other than id should be populated by the caller
     * @return the generated job id, or {@code null} if append failed
     */
    public String saveWithNextId(Job job) {
        String generatedId = FileManager.appendWithGeneratedId(filePath, Job.CSV_HEADER, "J", id -> {
            job.setJobId(id);
            return job.toCsvRow();
        });
        if (generatedId != null) {
            job.setJobId(generatedId);
        }
        return generatedId;
    }

    /**
     * Replaces the CSV row matching {@link Job#getJobId()} on the updated object.
     *
     * @param updated job with new field values and the same job id
     */
    public void update(Job updated) {
        FileManager.updateRows(filePath, Job.CSV_HEADER, rows -> {
            List<String> newRows = new ArrayList<>();
            for (String row : rows) {
                Job j = Job.fromCsvRow(row);
                if (j != null && j.getJobId().equals(updated.getJobId()))
                    newRows.add(updated.toCsvRow());
                else newRows.add(row);
            }
            return newRows;
        });
    }

    /**
     * Computes the next available job id with prefix {@code J} without writing a row.
     *
     * @return the next id string (for example {@code J001})
     */
    public String generateNextId() {
        return FileManager.generateNextId(filePath, "J");
    }
}
