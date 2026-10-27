package com.mudassir.railbook.service;

import com.mudassir.railbook.config.Constants;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.HexFormat;

/**
 * Cryptographic utility class for password hashing, salting, and constant-time verification.
 * Employs PBKDF2WithHmacSHA256 with 65,536 iterations and per-user cryptographically
 * secure random salts.
 *
 * @author Mudassir Ahmed
 */
public final class PasswordUtil {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private PasswordUtil() {
        // Utility class
    }

    /**
     * Generates a cryptographically secure random salt hex string.
     *
     * @return hex-encoded salt string
     */
    public static String generateSalt() {
        byte[] salt = new byte[Constants.SALT_BYTE_LENGTH];
        SECURE_RANDOM.nextBytes(salt);
        return HexFormat.of().formatHex(salt);
    }

    /**
     * Hashes the given raw password using PBKDF2WithHmacSHA256 and the provided salt.
     *
     * @param password raw password characters
     * @param saltHex  hex-encoded per-user salt
     * @return hex-encoded password hash
     */
    public static String hashPassword(String password, String saltHex) {
        if (password == null || saltHex == null) {
            throw new IllegalArgumentException("Password and salt cannot be null");
        }
        byte[] salt = HexFormat.of().parseHex(saltHex);
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, Constants.HASH_ITERATIONS, Constants.HASH_KEY_LENGTH);
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = skf.generateSecret(spec).getEncoded();
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Error generating password hash: " + e.getMessage(), e);
        } finally {
            spec.clearPassword();
        }
    }

    /**
     * Verifies a raw password against the stored salt and password hash using a
     * constant-time comparison to guard against side-channel timing attacks.
     *
     * @param rawPassword        candidate password provided by user
     * @param storedSaltHex      salt stored for user
     * @param expectedHashHex    expected password hash stored in database
     * @return true if password matches, false otherwise
     */
    public static boolean verifyPassword(String rawPassword, String storedSaltHex, String expectedHashHex) {
        if (rawPassword == null || storedSaltHex == null || expectedHashHex == null) {
            return false;
        }
        try {
            String computedHashHex = hashPassword(rawPassword, storedSaltHex);
            byte[] expectedBytes = HexFormat.of().parseHex(expectedHashHex);
            byte[] computedBytes = HexFormat.of().parseHex(computedHashHex);
            return MessageDigest.isEqual(expectedBytes, computedBytes);
        } catch (Exception e) {
            return false;
        }
    }
}
