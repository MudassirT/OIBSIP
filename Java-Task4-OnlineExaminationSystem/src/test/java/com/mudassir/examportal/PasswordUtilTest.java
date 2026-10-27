package com.mudassir.examportal;

import com.mudassir.examportal.service.PasswordUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Password Hashing & PBKDF2 Security Tests")
class PasswordUtilTest {

    @Test
    @DisplayName("Should generate non-empty unique random salts")
    void testSaltGeneration() {
        String salt1 = PasswordUtil.generateSalt();
        String salt2 = PasswordUtil.generateSalt();

        assertNotNull(salt1);
        assertNotNull(salt2);
        assertEquals(32, salt1.length()); // 16 bytes = 32 hex chars
        assertEquals(32, salt2.length());
        assertNotEquals(salt1, salt2, "Two generated salts should be unique");
    }

    @Test
    @DisplayName("Should correctly hash and verify password")
    void testPasswordHashAndVerify() {
        char[] password = "SecurePassword@123".toCharArray();
        String salt = PasswordUtil.generateSalt();

        String hash = PasswordUtil.hashPassword(password, salt);
        assertNotNull(hash);
        assertEquals(64, hash.length()); // 256 bits = 32 bytes = 64 hex chars

        // Verification with correct password
        assertTrue(PasswordUtil.verifyPassword(password, salt, hash),
                "Correct password should verify successfully");

        // Verification with wrong password
        assertFalse(PasswordUtil.verifyPassword("WrongPassword@123".toCharArray(), salt, hash),
                "Incorrect password must fail verification");

        // Verification with null/empty
        assertFalse(PasswordUtil.verifyPassword(null, salt, hash));
        assertFalse(PasswordUtil.verifyPassword("".toCharArray(), salt, hash));
    }

    @Test
    @DisplayName("Different salts must produce different hashes for identical passwords")
    void testDifferentSaltsProduceDifferentHashes() {
        char[] password = "Password@999".toCharArray();
        String salt1 = PasswordUtil.generateSalt();
        String salt2 = PasswordUtil.generateSalt();

        String hash1 = PasswordUtil.hashPassword(password, salt1);
        String hash2 = PasswordUtil.hashPassword(password, salt2);

        assertNotEquals(hash1, hash2, "Identical passwords with different salts must yield different hashes");
    }
}
