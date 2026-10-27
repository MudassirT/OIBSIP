package com.mudassir.railbook.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests verifying PBKDF2 password hashing, salt generation, and constant-time verification.
 *
 * @author Mudassir Ahmed
 */
class PasswordUtilTest {

    @Test
    @DisplayName("Password hashing produces unique 64-character hex strings for different salts")
    void testHashingWithDifferentSalts() {
        String salt1 = PasswordUtil.generateSalt();
        String salt2 = PasswordUtil.generateSalt();

        assertNotNull(salt1);
        assertNotNull(salt2);
        assertNotEquals(salt1, salt2, "Cryptographic salts should be unique per invocation");

        String hash1 = PasswordUtil.hashPassword("Secret@123", salt1);
        String hash2 = PasswordUtil.hashPassword("Secret@123", salt2);

        assertNotNull(hash1);
        assertNotNull(hash2);
        assertEquals(64, hash1.length(), "SHA-256 / 256-bit hash should be 64 hex characters");
        assertNotEquals(hash1, hash2, "Hashes of the same password with different salts must differ");
    }

    @Test
    @DisplayName("Password verification succeeds with correct credentials and fails with incorrect")
    void testVerifyPassword() {
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword("StrongP@ssw0rd", salt);

        assertTrue(PasswordUtil.verifyPassword("StrongP@ssw0rd", salt, hash),
                "Correct password should verify successfully");
        assertFalse(PasswordUtil.verifyPassword("WrongPassword", salt, hash),
                "Incorrect password must fail verification");
        assertFalse(PasswordUtil.verifyPassword("strongp@ssw0rd", salt, hash),
                "Password comparison must be case-sensitive");
    }

    @Test
    @DisplayName("Password verification handles null inputs gracefully without throwing exceptions")
    void testVerifyPasswordNullHandling() {
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword("Test@123", salt);

        assertFalse(PasswordUtil.verifyPassword(null, salt, hash));
        assertFalse(PasswordUtil.verifyPassword("Test@123", null, hash));
        assertFalse(PasswordUtil.verifyPassword("Test@123", salt, null));
    }
}
