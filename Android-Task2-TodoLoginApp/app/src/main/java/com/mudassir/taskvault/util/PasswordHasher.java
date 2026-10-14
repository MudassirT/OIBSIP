package com.mudassir.taskvault.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Utility class providing cryptographic password hashing and verification using
 * PBKDF2WithHmacSHA256 with salted hashes and constant-time comparison.
 * Plain-text passwords and password hashes are never logged.
 */
public final class PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATION_COUNT = 12000; // Exceeds minimum 10,000 iterations requirement
    private static final int KEY_LENGTH = 256;        // 256-bit derived key
    private static final int SALT_BYTE_LENGTH = 16;   // 128-bit cryptographically secure salt

    private PasswordHasher() {
        // Private constructor to prevent instantiation of utility class
    }

    /**
     * Generates a cryptographically strong random 16-byte salt, returned as Base64 string.
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_BYTE_LENGTH];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Hashes a password with a given salt using PBKDF2WithHmacSHA256.
     *
     * @param password   The plain text password.
     * @param saltBase64 The Base64-encoded salt.
     * @return Base64-encoded password hash.
     */
    public static String hashPassword(String password, String saltBase64) {
        if (password == null || saltBase64 == null) {
            throw new IllegalArgumentException("Password and salt must not be null");
        }

        try {
            byte[] salt = Base64.getDecoder().decode(saltBase64);
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error occurred while generating password hash", e);
        }
    }

    /**
     * Verifies a plain-text password against a stored Base64 hash and salt
     * using constant-time byte comparison (MessageDigest.isEqual) to prevent timing attacks.
     *
     * @param plainPassword      The candidate password to verify.
     * @param storedHashBase64   The stored Base64 hash from the database.
     * @param storedSaltBase64   The stored Base64 salt from the database.
     * @return true if password matches, false otherwise.
     */
    public static boolean verifyPassword(String plainPassword, String storedHashBase64, String storedSaltBase64) {
        if (plainPassword == null || storedHashBase64 == null || storedSaltBase64 == null) {
            return false;
        }

        try {
            byte[] expectedHash = Base64.getDecoder().decode(storedHashBase64);
            byte[] computedHash = Base64.getDecoder().decode(hashPassword(plainPassword, storedSaltBase64));

            // MessageDigest.isEqual performs constant-time comparison
            return MessageDigest.isEqual(expectedHash, computedHash);
        } catch (Exception e) {
            return false;
        }
    }
}
