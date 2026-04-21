package com.ta.model;

import java.util.ArrayList;
import java.util.List;

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

    public Job() {}

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

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public String getMoUserId() { return moUserId; }
    public void setMoUserId(String moUserId) { this.moUserId = moUserId; }

    public String getModuleCode() { return moduleCode; }
    public void setModuleCode(String moduleCode) { this.moduleCode = moduleCode; }

    public String getModuleName() { return moduleName; }
    public void setModuleName(String moduleName) { this.moduleName = moduleName; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(String requiredSkills) { this.requiredSkills = requiredSkills; }

    // backward compatibility for JSPs
    public String getRequirements() { return requiredSkills; }

    public int getVacancies() { return vacancies; }
    public void setVacancies(int vacancies) { this.vacancies = vacancies; }

    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }

    public String getWorkingPeriod() { return workingPeriod; }
    public void setWorkingPeriod(String workingPeriod) { this.workingPeriod = workingPeriod; }

    public String getKeyDuties() { return keyDuties; }
    public void setKeyDuties(String keyDuties) { this.keyDuties = keyDuties; }

    public String getEligibility() { return eligibility; }
    public void setEligibility(String eligibility) { this.eligibility = eligibility; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedDate() { return createdDate; }
    public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }

    public String toCsvRow() {
        return String.join(",", jobId, moUserId, q(moduleCode), q(moduleName), q(jobTitle), q(description),
            q(requiredSkills), String.valueOf(vacancies), deadline, q(workingPeriod), q(keyDuties), q(eligibility), status, createdDate);
    }

    public static final String CSV_HEADER =
        "jobId,moUserId,moduleCode,moduleName,jobTitle,description,requiredSkills,vacancies,deadline,workingPeriod,keyDuties,eligibility,status,createdDate";

    public static Job fromCsvRow(String row) {
        String[] f = parseCsv(row);
        // Handle old CSV format which had 9 fields
        if (f.length == 9) {
            try {
                return new Job(f[0], f[1], "", f[2], "", f[3], f[4],
                    Integer.parseInt(f[5]), f[6], "", "", "", f[7], f[8]);
            } catch (NumberFormatException e) { return null; }
        }
        
        if (f.length < 14) return null;
        
        try {
            return new Job(f[0], f[1], f[2], f[3], f[4], f[5], f[6],
                Integer.parseInt(f[7]), f[8], f[9], f[10], f[11], f[12], f[13]);
        } catch (NumberFormatException e) { return null; }
    }

    private static String q(String s) {
        if (s == null) return "\"\"";
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }

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
}
