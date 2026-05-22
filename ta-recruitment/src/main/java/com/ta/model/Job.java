package com.ta.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Domain model representing a TA job posting created by a Module Organiser.
 * Captures module details, role requirements, vacancy count, deadlines, and
 * publication status. Supports both legacy (9-field) and current (14-field) CSV
 * formats for backward-compatible persistence.
 */
public class Job {
    private String jobId;
    private String moUserId;
    private String moduleCode;
    private String moduleName;
    private String jobTitle;
    private String description;
    private String requiredSkills;
    private int vacancies;
    private String deadline;
    private String workingPeriod;
    private String keyDuties;
    private String eligibility;
    private String status;
    private String createdDate;

    /** Creates an empty job with all fields unset. */
    public Job() {}

    /**
     * Creates a job with all fields populated.
     *
     * @param jobId          unique identifier for this job posting
     * @param moUserId       user id of the Module Organiser who created the job
     * @param moduleCode     academic module code (e.g. {@code EBU6304})
     * @param moduleName     human-readable module title
     * @param jobTitle       short title for the TA role
     * @param description    full job description
     * @param requiredSkills comma-separated or free-text skill requirements
     * @param vacancies      number of TA positions available
     * @param deadline       application deadline (ISO date string)
     * @param workingPeriod  expected working period for the role
     * @param keyDuties      summary of primary duties
     * @param eligibility    eligibility criteria for applicants
     * @param status         publication status (e.g. {@code OPEN}, {@code CLOSED})
     * @param createdDate    date the job was created (ISO date string)
     */
    public Job(String jobId, String moUserId, String moduleCode, String moduleName,
               String jobTitle, String description, String requiredSkills, int vacancies,
               String deadline, String workingPeriod, String keyDuties, String eligibility,
               String status, String createdDate) {
        this.jobId = jobId;
        this.moUserId = moUserId;
        this.moduleCode = moduleCode;
        this.moduleName = moduleName;
        this.jobTitle = jobTitle;
        this.description = description;
        this.requiredSkills = requiredSkills;
        this.vacancies = vacancies;
        this.deadline = deadline;
        this.workingPeriod = workingPeriod;
        this.keyDuties = keyDuties;
        this.eligibility = eligibility;
        this.status = status;
        this.createdDate = createdDate;
    }

    /** @return the unique job identifier */
    public String getJobId() { return jobId; }

    /** @param jobId the unique job identifier to set */
    public void setJobId(String jobId) { this.jobId = jobId; }

    /** @return the user id of the Module Organiser who owns this job */
    public String getMoUserId() { return moUserId; }

    /** @param moUserId the Module Organiser user id to set */
    public void setMoUserId(String moUserId) { this.moUserId = moUserId; }

    /** @return the academic module code */
    public String getModuleCode() { return moduleCode; }

    /** @param moduleCode the module code to set */
    public void setModuleCode(String moduleCode) { this.moduleCode = moduleCode; }

    /** @return the human-readable module name */
    public String getModuleName() { return moduleName; }

    /** @param moduleName the module name to set */
    public void setModuleName(String moduleName) { this.moduleName = moduleName; }

    /** @return the short job title */
    public String getJobTitle() { return jobTitle; }

    /** @param jobTitle the job title to set */
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    /** @return the full job description */
    public String getDescription() { return description; }

    /** @param description the job description to set */
    public void setDescription(String description) { this.description = description; }

    /** @return the required skills or qualifications for the role */
    public String getRequiredSkills() { return requiredSkills; }

    /** @param requiredSkills the required skills to set */
    public void setRequiredSkills(String requiredSkills) { this.requiredSkills = requiredSkills; }

    /**
     * Alias for {@link #getRequiredSkills()} retained for JSP backward compatibility.
     *
     * @return the required skills or qualifications for the role
     */
    // backward compatibility for JSPs
    public String getRequirements() { return requiredSkills; }

    /** @return the number of TA vacancies for this job */
    public int getVacancies() { return vacancies; }

    /** @param vacancies the vacancy count to set */
    public void setVacancies(int vacancies) { this.vacancies = vacancies; }

    /** @return the application deadline (ISO date string) */
    public String getDeadline() { return deadline; }

    /** @param deadline the application deadline to set */
    public void setDeadline(String deadline) { this.deadline = deadline; }

    /** @return the expected working period for the role */
    public String getWorkingPeriod() { return workingPeriod; }

    /** @param workingPeriod the working period to set */
    public void setWorkingPeriod(String workingPeriod) { this.workingPeriod = workingPeriod; }

    /** @return a summary of key duties for the TA role */
    public String getKeyDuties() { return keyDuties; }

    /** @param keyDuties the key duties description to set */
    public void setKeyDuties(String keyDuties) { this.keyDuties = keyDuties; }

    /** @return eligibility criteria for applicants */
    public String getEligibility() { return eligibility; }

    /** @param eligibility the eligibility criteria to set */
    public void setEligibility(String eligibility) { this.eligibility = eligibility; }

    /** @return the publication status (e.g. {@code OPEN}, {@code CLOSED}) */
    public String getStatus() { return status; }

    /** @param status the publication status to set */
    public void setStatus(String status) { this.status = status; }

    /** @return the date this job was created (ISO date string) */
    public String getCreatedDate() { return createdDate; }

    /** @param createdDate the creation date to set */
    public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }

    /**
     * Serialises this job to a single CSV row (no header).
     * Text fields that may contain commas are wrapped in double quotes.
     *
     * @return comma-separated field values matching {@link #CSV_HEADER}
     */
    public String toCsvRow() {
        return String.join(",", jobId, moUserId, q(moduleCode), q(moduleName), q(jobTitle), q(description),
            q(requiredSkills), String.valueOf(vacancies), deadline, q(workingPeriod), q(keyDuties), q(eligibility), status, createdDate);
    }

    /** CSV column header for job persistence files. */
    public static final String CSV_HEADER =
        "jobId,moUserId,moduleCode,moduleName,jobTitle,description,requiredSkills,vacancies,deadline,workingPeriod,keyDuties,eligibility,status,createdDate";

    /**
     * Parses a CSV row into a {@link Job} instance.
     * Accepts both the legacy 9-field format and the current 14-field format.
     * Rows that parse successfully but lack displayable content are discarded.
     *
     * @param row a single CSV line (without header)
     * @return a populated job, or {@code null} if the row is invalid or not displayable
     */
    public static Job fromCsvRow(String row) {
        String[] f = parseCsv(row);
        // Handle old CSV format which had 9 fields
        if (f.length == 9) {
            try {
                Job job = new Job(f[0], f[1], "", f[2], "", f[3], f[4],
                    Integer.parseInt(f[5]), f[6], "", "", "", f[7], f[8]);
                return isDisplayable(job) ? job : null;
            } catch (NumberFormatException e) { return null; }
        }
        
        if (f.length < 14) return null;
        
        try {
            Job job = new Job(f[0], f[1], f[2], f[3], f[4], f[5], f[6],
                Integer.parseInt(f[7]), f[8], f[9], f[10], f[11], f[12], f[13]);
            return isDisplayable(job) ? job : null;
        } catch (NumberFormatException e) { return null; }
    }

    private static String q(String s) {
        if (s == null) return "\"\"";
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }

    /**
     * Parses a CSV line into individual field values, respecting quoted fields
     * and escaped double quotes ({@code ""} inside quotes).
     *
     * @param row a single CSV line
     * @return array of unquoted field strings
     */
    public static String[] parseCsv(String row) {
        List<String> fields = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQ = false;
        for (int i = 0; i < row.length(); i++) {
            char c = row.charAt(i);
            if (c == '"') {
                if (inQ && i + 1 < row.length() && row.charAt(i + 1) == '"') {
                    sb.append('"'); i++;
                } else { inQ = !inQ; }
            } else if (c == ',' && !inQ) {
                fields.add(sb.toString()); sb.setLength(0);
            } else { sb.append(c); }
        }
        fields.add(sb.toString());
        return fields.toArray(new String[0]);
    }

    private static boolean isDisplayable(Job job) {
        if (job == null) {
            return false;
        }

        return hasText(job.getModuleCode())
                || hasText(job.getModuleName())
                || hasText(job.getJobTitle())
                || hasText(job.getDescription())
                || hasText(job.getRequiredSkills());
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
