package com.predistock.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for secure password hashing and verification using SHA-256.
 * Demonstrates static utility pattern and encapsulation of security logic.
 */
public final class PasswordUtil {

    // Private constructor to prevent instantiation
    private PasswordUtil() {
        throw novelUnsupportedOperationException();
    }

    private static UnsupportedOperationException novelUnsupportedOperationException() {
        return new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Hashes a raw plain-text password using SHA-256 algorithm.
     *
     * @param raw the raw password string
     * @return hex-encoded SHA-256 hash string
     */
    public static String hash(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encodedhash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Verifies whether a raw plain-text password matches an existing SHA-256 hash.
     *
     * @param raw  the plain-text password to verify
     * @param hash the stored hash to compare against
     * @return true if password matches, false otherwise
     */
    public static boolean verify(String raw, String hash) {
        if (raw == null || hash == null) {
            return false;
        }
        String calculatedHash = hash(raw);
        return calculatedHash.equalsIgnoreCase(hash);
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}

