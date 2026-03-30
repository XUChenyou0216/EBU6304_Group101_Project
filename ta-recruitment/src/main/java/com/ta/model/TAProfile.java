package com.ta.model;

public class TAProfile {
    private String userId;
    private String studentId;
    private String fullName;
    private String programme;
    private String yearOfStudy;
    private String phone;
    private String cvFilePath;
    /** Semicolon-separated skill tags */
    private String skills;
    /** e.g. 3.85 */
    private String gpa;
    /** e.g. 2024/25 */
    private String academicYear;

    public TAProfile() {}

    public TAProfile(String userId, String studentId, String fullName,
                     String programme, String yearOfStudy, String phone,
                     String cvFilePath) {
        this(userId, studentId, fullName, programme, yearOfStudy, phone, cvFilePath, null, null, null);
    }

    public TAProfile(String userId, String studentId, String fullName,
                     String programme, String yearOfStudy, String phone,
                     String cvFilePath, String skills) {
        this(userId, studentId, fullName, programme, yearOfStudy, phone, cvFilePath, skills, null, null);
    }

    public TAProfile(String userId, String studentId, String fullName,
                     String programme, String yearOfStudy, String phone,
                     String cvFilePath, String skills, String gpa, String academicYear) {
        this.userId = userId;
        this.studentId = studentId;
        this.fullName = fullName;
        this.programme = programme;
        this.yearOfStudy = yearOfStudy;
        this.phone = phone;
        this.cvFilePath = cvFilePath;
        this.skills = skills;
        this.gpa = gpa;
        this.academicYear = academicYear;
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
    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }
    public String getGpa() { return gpa; }
    public void setGpa(String gpa) { this.gpa = gpa; }
    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public String toCsvRow() {
        return String.join(",", userId, studentId, q(fullName), q(programme),
            yearOfStudy, phone, cvFilePath != null ? cvFilePath : "",
            skills != null && !skills.isEmpty() ? q(skills) : "",
            gpa != null && !gpa.isEmpty() ? gpa : "",
            academicYear != null && !academicYear.isEmpty() ? q(academicYear) : "");
    }

    public static final String CSV_HEADER =
        "userId,studentId,fullName,programme,yearOfStudy,phone,cvFilePath,skills,gpa,academicYear";

    public static TAProfile fromCsvRow(String row) {
        String[] f = Job.parseCsv(row);
        if (f.length < 7) return null;
        String skills = f.length >= 8 ? f[7] : "";
        String gpa = f.length >= 9 ? f[8] : "";
        String academicYear = f.length >= 10 ? f[9] : "";
        return new TAProfile(f[0], f[1], f[2], f[3], f[4], f[5], f[6],
            skills != null && !skills.isEmpty() ? skills : null,
            gpa != null && !gpa.isEmpty() ? gpa : null,
            academicYear != null && !academicYear.isEmpty() ? academicYear : null);
    }

    private static String q(String s) {
        if (s == null) return "\"\"";
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }
}
