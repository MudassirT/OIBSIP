package com.mudassir.taskvault;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.mudassir.taskvault.util.PasswordHasher;

import org.junit.Test;

/**
 * Unit tests validating PBKDF2WithHmacSHA256 password hashing, salt uniqueness,
 * and constant-time verification logic.
 */
public class PasswordHasherTest {

    @Test
    public void testSaltGenerationIsNonEmptyAndUnique() {
        String salt1 = PasswordHasher.generateSalt();
        String salt2 = PasswordHasher.generateSalt();

        assertNotNull(salt1);
        assertNotNull(salt2);
        assertFalse(salt1.isEmpty());
        assertFalse(salt2.isEmpty());
        assertNotEquals("Each generated salt must be cryptographically unique", salt1, salt2);
    }

    @Test
    public void testHashPasswordGeneratesConsistentHashForSameSalt() {
        String password = "SecurePassword123";
        String salt = PasswordHasher.generateSalt();

        String hash1 = PasswordHasher.hashPassword(password, salt);
        String hash2 = PasswordHasher.hashPassword(password, salt);

        assertNotNull(hash1);
        assertFalse(hash1.isEmpty());
        assertEquals("Hashing the same password with the same salt must yield identical hash", hash1, hash2);
        assertNotEquals("Hash must not equal plain password", password, hash1);
    }

    @Test
    public void testDifferentSaltsProduceDifferentHashesForSamePassword() {
        String password = "SecurePassword123";
        String salt1 = PasswordHasher.generateSalt();
        String salt2 = PasswordHasher.generateSalt();

        String hash1 = PasswordHasher.hashPassword(password, salt1);
        String hash2 = PasswordHasher.hashPassword(password, salt2);

        assertNotEquals("Identical passwords with different salts must yield different hashes", hash1, hash2);
    }

    @Test
    public void testVerifyPasswordReturnsTrueForCorrectPassword() {
        String password = "MySecretTaskPassword2026!";
        String salt = PasswordHasher.generateSalt();
        String storedHash = PasswordHasher.hashPassword(password, salt);

        boolean isMatch = PasswordHasher.verifyPassword(password, storedHash, salt);
        assertTrue("Verification must succeed when password matches", isMatch);
    }

    @Test
    public void testVerifyPasswordReturnsFalseForIncorrectPassword() {
        String password = "MySecretTaskPassword2026!";
        String wrongPassword = "WrongPassword123";
        String salt = PasswordHasher.generateSalt();
        String storedHash = PasswordHasher.hashPassword(password, salt);

        boolean isMatch = PasswordHasher.verifyPassword(wrongPassword, storedHash, salt);
        assertFalse("Verification must fail when password does not match", isMatch);
    }

    @Test
    public void testVerifyPasswordHandlesNullsSafely() {
        assertFalse(PasswordHasher.verifyPassword(null, "someHash", "someSalt"));
        assertFalse(PasswordHasher.verifyPassword("password", null, "someSalt"));
        assertFalse(PasswordHasher.verifyPassword("password", "someHash", null));
    }
}
