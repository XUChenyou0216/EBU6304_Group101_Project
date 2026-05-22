package com.ta.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility for hashing and verifying user passwords using SHA-256.
 * <p>
 * Passwords are stored as lowercase hexadecimal digests. This class does not use salting;
 * it is intended for demonstration or legacy compatibility within the TA Recruitment system.
 * </p>
 */
public class PasswordUtil {

    /**
     * Computes the SHA-256 hash of a plaintext password.
     *
     * @param password the plaintext password to hash (must not be {@code null})
     * @return the 64-character lowercase hexadecimal digest
     * @throws RuntimeException if the SHA-256 algorithm is unavailable on this JVM
     */
    public static String hash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    /**
     * Verifies that a plaintext password matches a stored hash.
     *
     * @param password the plaintext password to check
     * @param hash     the previously stored SHA-256 hex digest
     * @return {@code true} if the password hashes to the same value as {@code hash}
     */
    public static boolean verify(String password, String hash) {
        return hash(password).equals(hash);
    }
}
