package com.ta.model;

public class User {
    private String userId;
    private String username;
    private String passwordHash;
    private String role;         // "TA", "MO", "ADMIN"
    private String email;
    private String securityQuestion;
    private String securityAnswer;
    private String status;       // "ACTIVE", "SUSPENDED"

    public User() {}

    public User(String userId, String username, String passwordHash,
                String role, String email, String securityQuestion,
                String securityAnswer, String status) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.email = email;
        this.securityQuestion = securityQuestion;
        this.securityAnswer = securityAnswer;
        this.status = status;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSecurityQuestion() { return securityQuestion; }
    public void setSecurityQuestion(String q) { this.securityQuestion = q; }
    public String getSecurityAnswer() { return securityAnswer; }
    public void setSecurityAnswer(String a) { this.securityAnswer = a; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String toCsvRow() {
        return String.join(",", userId, username, passwordHash, role, email,
            securityQuestion, securityAnswer, status);
    }

    public static final String CSV_HEADER =
        "userId,username,passwordHash,role,email,securityQuestion,securityAnswer,status";

    public static User fromCsvRow(String row) {
        String[] f = row.split(",", -1);
        if (f.length < 8) return null;
        return new User(f[0], f[1], f[2], f[3], f[4], f[5], f[6], f[7]);
    }
}
