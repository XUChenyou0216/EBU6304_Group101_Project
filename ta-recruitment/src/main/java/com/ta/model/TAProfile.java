package com.ta.model;

public class TAProfile {
    private String userId;
    private String studentId;
    private String fullName;
    private String programme;
    private String yearOfStudy;
    private String phone;
    private String cvFilePath;

    public TAProfile() {}

    public TAProfile(String userId, String studentId, String fullName,
                     String programme, String yearOfStudy, String phone,
                     String cvFilePath) {
        this.userId = userId;
        this.studentId = studentId;
        this.fullName = fullName;
        this.programme = programme;
        this.yearOfStudy = yearOfStudy;
        this.phone = phone;
        this.cvFilePath = cvFilePath;
    }

    public String getUserId() { return userId; }
    public void setUserId(String id) { this.userId = id; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String id) { this.studentId = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String n) { this.fullName = n; }
    public String getProgramme() { return programme; }
    public void setProgramme(String p) { this.programme = p; }
    public String getYearOfStudy() { return yearOfStudy; }
    public void setYearOfStudy(String y) { this.yearOfStudy = y; }
    public String getPhone() { return phone; }
    public void setPhone(String p) { this.phone = p; }
    public String getCvFilePath() { return cvFilePath; }
    public void setCvFilePath(String path) { this.cvFilePath = path; }

    public String toCsvRow() {
        return String.join(",", userId, studentId, fullName, programme,
            yearOfStudy, phone, cvFilePath != null ? cvFilePath : "");
    }

    public static final String CSV_HEADER =
        "userId,studentId,fullName,programme,yearOfStudy,phone,cvFilePath";

    public static TAProfile fromCsvRow(String row) {
        String[] f = row.split(",", -1);
        if (f.length < 7) return null;
        return new TAProfile(f[0], f[1], f[2], f[3], f[4], f[5], f[6]);
    }
}
