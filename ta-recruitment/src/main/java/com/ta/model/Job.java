package com.ta.model;

import java.util.ArrayList;
import java.util.List;

public class Job {
    private String jobId;
    private String moUserId;
    private String moduleName;
    private String description;
    private String requirements;
    private int vacancies;
    private String deadline;       // yyyy-MM-dd
    private String status;         // "OPEN", "CLOSED", "FILLED"
    private String createdDate;

    public Job() {}

    public Job(String jobId, String moUserId, String moduleName,
               String description, String requirements, int vacancies,
               String deadline, String status, String createdDate) {
        this.jobId = jobId;
        this.moUserId = moUserId;
        this.moduleName = moduleName;
        this.description = description;
        this.requirements = requirements;
        this.vacancies = vacancies;
        this.deadline = deadline;
        this.status = status;
        this.createdDate = createdDate;
    }

    public String getJobId() { return jobId; }
    public void setJobId(String id) { this.jobId = id; }
    public String getMoUserId() { return moUserId; }
    public void setMoUserId(String id) { this.moUserId = id; }
    public String getModuleName() { return moduleName; }
    public void setModuleName(String n) { this.moduleName = n; }
    public String getDescription() { return description; }
    public void setDescription(String d) { this.description = d; }
    public String getRequirements() { return requirements; }
    public void setRequirements(String r) { this.requirements = r; }
    public int getVacancies() { return vacancies; }
    public void setVacancies(int v) { this.vacancies = v; }
    public String getDeadline() { return deadline; }
    public void setDeadline(String d) { this.deadline = d; }
    public String getStatus() { return status; }
    public void setStatus(String s) { this.status = s; }
    public String getCreatedDate() { return createdDate; }
    public void setCreatedDate(String d) { this.createdDate = d; }

    public String toCsvRow() {
        return String.join(",", jobId, moUserId, q(moduleName), q(description),
            q(requirements), String.valueOf(vacancies), deadline, status, createdDate);
    }

    public static final String CSV_HEADER =
        "jobId,moUserId,moduleName,description,requirements,vacancies,deadline,status,createdDate";

    public static Job fromCsvRow(String row) {
        String[] f = parseCsv(row);
        if (f.length < 9) return null;
        try {
            return new Job(f[0], f[1], f[2], f[3], f[4],
                Integer.parseInt(f[5]), f[6], f[7], f[8]);
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
