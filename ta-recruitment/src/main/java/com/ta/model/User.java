package com.ta.model;

/**
 * Domain model representing a user account in the TA recruitment system.
 * Users may be Teaching Assistants ({@code TA}), Module Organisers ({@code MO}),
 * or administrators ({@code ADMIN}). Account state is tracked via {@code status}
 * (e.g. {@code ACTIVE}, {@code SUSPENDED}). Instances are persisted as CSV rows.
 */
public class User {
    private String userId;
    private String username;
    private String passwordHash;
    private String role;         // "TA", "MO", "ADMIN"
    private String email;
    private String securityQuestion;
    private String securityAnswer;
    private String status;       // "ACTIVE", "SUSPENDED"

    /** Creates an empty user with all fields unset. */
    public User() {}

    /**
     * Creates a user with all fields populated.
     *
     * @param userId           unique identifier for this user
     * @param username         login name
     * @param passwordHash     stored password hash (never plain text)
     * @param role             user role ({@code TA}, {@code MO}, or {@code ADMIN})
     * @param email            contact email address
     * @param securityQuestion security question for password recovery
     * @param securityAnswer   answer to the security question
     * @param status           account status ({@code ACTIVE} or {@code SUSPENDED})
     */
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

    /** @return the unique user identifier */
    public String getUserId() { return userId; }

    /** @param userId the unique user identifier to set */
    public void setUserId(String userId) { this.userId = userId; }

    /** @return the login username */
    public String getUsername() { return username; }

    /** @param username the login username to set */
    public void setUsername(String username) { this.username = username; }

    /** @return the stored password hash */
    public String getPasswordHash() { return passwordHash; }

    /** @param passwordHash the password hash to set */
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    /** @return the user role ({@code TA}, {@code MO}, or {@code ADMIN}) */
    public String getRole() { return role; }

    /** @param role the user role to set */
    public void setRole(String role) { this.role = role; }

    /** @return the contact email address */
    public String getEmail() { return email; }

    /** @param email the contact email address to set */
    public void setEmail(String email) { this.email = email; }

    /** @return the security question used for password recovery */
    public String getSecurityQuestion() { return securityQuestion; }

    /** @param q the security question to set */
    public void setSecurityQuestion(String q) { this.securityQuestion = q; }

    /** @return the security answer used for password recovery */
    public String getSecurityAnswer() { return securityAnswer; }

    /** @param a the security answer to set */
    public void setSecurityAnswer(String a) { this.securityAnswer = a; }

    /** @return the account status ({@code ACTIVE} or {@code SUSPENDED}) */
    public String getStatus() { return status; }

    /** @param status the account status to set */
    public void setStatus(String status) { this.status = status; }

    /**
     * Serialises this user to a single CSV row (no header).
     *
     * @return comma-separated field values matching {@link #CSV_HEADER}
     */
    public String toCsvRow() {
        return String.join(",", userId, username, passwordHash, role, email,
            securityQuestion, securityAnswer, status);
    }

    /** CSV column header for user persistence files. */
    public static final String CSV_HEADER =
        "userId,username,passwordHash,role,email,securityQuestion,securityAnswer,status";

    /**
     * Parses a CSV row into a {@link User} instance.
     *
     * @param row a single CSV line (without header)
     * @return a populated user, or {@code null} if the row has fewer than eight fields
     */
    public static User fromCsvRow(String row) {
        String[] f = row.split(",", -1);
        if (f.length < 8) return null;
        return new User(f[0], f[1], f[2], f[3], f[4], f[5], f[6], f[7]);
    }
}
