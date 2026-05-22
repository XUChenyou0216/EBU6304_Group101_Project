package com.ta.model;

/**
 * Domain model representing extended profile information for a Teaching Assistant.
 * Linked to a {@link User} account via {@code userId}, this profile stores academic
 * details, contact information, and an optional CV file path. Instances are persisted
 * as CSV rows.
 */
public class TAProfile {
    private String userId;
    private String studentId;
    private String fullName;
    private String programme;
    private String yearOfStudy;
    private String phone;
    private String cvFilePath;

    /** Creates an empty TA profile with all fields unset. */
    public TAProfile() {}

    /**
     * Creates a TA profile with all fields populated.
     *
     * @param userId      id of the linked {@link User} account
     * @param studentId   university student identifier
     * @param fullName    the TA's full legal name
     * @param programme   degree programme or course of study
     * @param yearOfStudy current year of study (e.g. {@code "2"})
     * @param phone       contact phone number
     * @param cvFilePath  filesystem path to the uploaded CV, or {@code null}
     */
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

    /** @return the linked user account id */
    public String getUserId() { return userId; }

    /** @param id the linked user account id to set */
    public void setUserId(String id) { this.userId = id; }

    /** @return the university student identifier */
    public String getStudentId() { return studentId; }

    /** @param id the student identifier to set */
    public void setStudentId(String id) { this.studentId = id; }

    /** @return the TA's full name */
    public String getFullName() { return fullName; }

    /** @param n the full name to set */
    public void setFullName(String n) { this.fullName = n; }

    /** @return the degree programme or course of study */
    public String getProgramme() { return programme; }

    /** @param p the programme to set */
    public void setProgramme(String p) { this.programme = p; }

    /** @return the current year of study */
    public String getYearOfStudy() { return yearOfStudy; }

    /** @param y the year of study to set */
    public void setYearOfStudy(String y) { this.yearOfStudy = y; }

    /** @return the contact phone number */
    public String getPhone() { return phone; }

    /** @param p the phone number to set */
    public void setPhone(String p) { this.phone = p; }

    /** @return the filesystem path to the uploaded CV, or {@code null} if none */
    public String getCvFilePath() { return cvFilePath; }

    /** @param path the CV file path to set */
    public void setCvFilePath(String path) { this.cvFilePath = path; }

    /**
     * Serialises this profile to a single CSV row (no header).
     *
     * @return comma-separated field values matching {@link #CSV_HEADER}
     */
    public String toCsvRow() {
        return String.join(",", userId, studentId, fullName, programme,
            yearOfStudy, phone, cvFilePath != null ? cvFilePath : "");
    }

    /** CSV column header for TA profile persistence files. */
    public static final String CSV_HEADER =
        "userId,studentId,fullName,programme,yearOfStudy,phone,cvFilePath";

    /**
     * Parses a CSV row into a {@link TAProfile} instance.
     *
     * @param row a single CSV line (without header)
     * @return a populated profile, or {@code null} if the row has fewer than seven fields
     */
    public static TAProfile fromCsvRow(String row) {
        String[] f = row.split(",", -1);
        if (f.length < 7) return null;
        return new TAProfile(f[0], f[1], f[2], f[3], f[4], f[5], f[6]);
    }
}
