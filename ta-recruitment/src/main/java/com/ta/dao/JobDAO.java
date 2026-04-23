package com.ta.dao;

import com.ta.model.Job;
import com.ta.util.FileManager;
import com.ta.util.JobDeadlineUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class JobDAO {
    private final String filePath;

    public JobDAO(String dataDir) { this.filePath = dataDir + "/jobs.csv"; }

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

    public Job findById(String jobId) {
        for (Job j : findAll()) {
            if (Objects.equals(j.getJobId(), jobId)) {
                return j;
            }
        }
        return null;
    }

    public List<Job> findOpen() {
        return findAll().stream().filter(j -> "OPEN".equals(j.getStatus())).collect(Collectors.toList());
    }

    public List<Job> findByMo(String moUserId) {
        return findAll().stream().filter(j -> j.getMoUserId().equals(moUserId)).collect(Collectors.toList());
    }

    public void save(Job job) {
        FileManager.appendRow(filePath, Job.CSV_HEADER, job.toCsvRow());
    }

    public void update(Job updated) {
        List<String> rows = FileManager.readAll(filePath);
        List<String> newRows = new ArrayList<>();
        for (String row : rows) {
            Job j = Job.fromCsvRow(row);
            if (j != null && j.getJobId().equals(updated.getJobId()))
                newRows.add(updated.toCsvRow());
            else newRows.add(row);
        }
        FileManager.writeAll(filePath, Job.CSV_HEADER, newRows);
    }

    public String generateNextId() {
        return FileManager.generateNextId(filePath, "J");
    }
}
