package com.mudassir.examportal.service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility for secure password hashing and verification using PBKDF2WithHmacSHA256
 * with per-user random salt and constant-time equality comparisons.
 */
public final class PasswordUtil {

    private static final Logger LOGGER = Logger.getLogger(PasswordUtil.class.getName());

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256; // bits
    private static final int SALT_BYTES = 16;  // 128 bits

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private PasswordUtil() {
        // Prevent instantiation
    }

    /**
     * Generates a cryptographically secure random salt encoded as a hexadecimal string.
     *
     * @return hex-encoded salt string
     */
    public static String generateSalt() {
        byte[] salt = new byte[SALT_BYTES];
        SECURE_RANDOM.nextBytes(salt);
        return bytesToHex(salt);
    }

    /**
     * Computes the PBKDF2 hash of the given password and salt.
     *
     * @param password character array of the password
     * @param saltHex  hex-encoded salt
     * @return hex-encoded hash string
     */
    public static String hashPassword(char[] password, String saltHex) {
        if (password == null || saltHex == null) {
            throw new IllegalArgumentException("Password and salt must not be null");
        }
        byte[] salt = hexToBytes(saltHex);
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = factory.generateSecret(spec).getEncoded();
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            LOGGER.log(Level.SEVERE, "Failed to compute PBKDF2 password hash", e);
            throw new IllegalStateException("Password hashing failure", e);
        } finally {
            spec.clearPassword();
        }
    }

    /**
     * Verifies whether a supplied password matches the expected stored hash in constant time.
     *
     * @param passwordSupplied character array of the password to check
     * @param saltHex          stored hex-encoded salt
     * @param expectedHashHex  stored hex-encoded hash
     * @return true if password matches, false otherwise
     */
    public static boolean verifyPassword(char[] passwordSupplied, String saltHex, String expectedHashHex) {
        if (passwordSupplied == null || saltHex == null || expectedHashHex == null) {
            return false;
        }
        try {
            String actualHashHex = hashPassword(passwordSupplied, saltHex);
            byte[] actualBytes = hexToBytes(actualHashHex);
            byte[] expectedBytes = hexToBytes(expectedHashHex);
            // Constant-time comparison to prevent timing attacks
            return MessageDigest.isEqual(actualBytes, expectedBytes);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error during password verification", e);
            return false;
        }
    }

    /**
     * Helper to convert byte array to hex string.
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Helper to convert hex string back to byte array.
     */
    public static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
