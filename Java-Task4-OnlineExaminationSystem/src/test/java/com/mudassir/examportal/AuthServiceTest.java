package com.mudassir.examportal;

import com.mudassir.examportal.config.AppConfig;
import com.mudassir.examportal.db.DatabaseManager;
import com.mudassir.examportal.db.UserDao;
import com.mudassir.examportal.model.User;
import com.mudassir.examportal.service.AuthService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Authentication Service & Rate Limiting Tests")
class AuthServiceTest {

    private File tempDb;
    private DatabaseManager dbManager;
    private AuthService authService;

    @BeforeEach
    void setUp() throws Exception {
        tempDb = File.createTempFile("auth_test_", ".db");
        tempDb.deleteOnExit();
        dbManager = new DatabaseManager("jdbc:sqlite:" + tempDb.getAbsolutePath());
        dbManager.initialize();

        UserDao userDao = new UserDao(dbManager);
        authService = new AuthService(userDao);
    }

    @AfterEach
    void tearDown() {
        if (tempDb != null && tempDb.exists()) {
            tempDb.delete();
        }
    }

    @Test
    @DisplayName("Should successfully authenticate seeded demo students")
    void testSuccessfulAuthentication() {
        AuthService.AuthResult result1 = authService.authenticate(
                AppConfig.DEMO_STUDENT1_USER,
                AppConfig.DEMO_STUDENT1_PASS.toCharArray()
        );
        assertTrue(result1.isSuccess(), "Student 1 should authenticate successfully");
        assertNotNull(result1.getUser());
        assertEquals(AppConfig.DEMO_STUDENT1_USER, result1.getUser().getUsername());

        AuthService.AuthResult result2 = authService.authenticate(
                AppConfig.DEMO_STUDENT2_USER,
                AppConfig.DEMO_STUDENT2_PASS.toCharArray()
        );
        assertTrue(result2.isSuccess(), "Student 2 should authenticate successfully");
    }

    @Test
    @DisplayName("Should reject invalid passwords with generic error")
    void testInvalidPasswordRejected() {
        AuthService.AuthResult result = authService.authenticate(
                AppConfig.DEMO_STUDENT1_USER,
                "WrongPassword@999".toCharArray()
        );
        assertFalse(result.isSuccess());
        assertEquals("Invalid username or password.", result.getErrorMessage());
        assertNull(result.getUser());
    }

    @Test
    @DisplayName("Should reject non-existent username with generic error without revealing field")
    void testNonExistentUserRejected() {
        AuthService.AuthResult result = authService.authenticate(
                "nonexistent_student",
                "SomePassword@123".toCharArray()
        );
        assertFalse(result.isSuccess());
        assertEquals("Invalid username or password.", result.getErrorMessage());
    }

    @Test
    @DisplayName("Should safely reject SQL-injection attacks like ' OR '1'='1")
    void testSqlInjectionSafeRejection() {
        String sqlInjectionUser = "' OR '1'='1";
        AuthService.AuthResult result = authService.authenticate(
                sqlInjectionUser,
                "arbitraryPassword".toCharArray()
        );
        assertFalse(result.isSuccess(), "SQL injection attack must fail authentication");
        assertEquals("Invalid username or password.", result.getErrorMessage());
    }

    @Test
    @DisplayName("Should reject empty or blank submissions")
    void testEmptySubmissionBlocked() {
        AuthService.AuthResult resultEmptyUser = authService.authenticate("", "Password@123".toCharArray());
        assertFalse(resultEmptyUser.isSuccess());
        assertEquals("Please enter both username and password.", resultEmptyUser.getErrorMessage());

        AuthService.AuthResult resultEmptyPass = authService.authenticate("student1", "".toCharArray());
        assertFalse(resultEmptyPass.isSuccess());
        assertEquals("Please enter both username and password.", resultEmptyPass.getErrorMessage());
    }

    @Test
    @DisplayName("Should lock the form for 30 seconds after 5 failed attempts")
    void testRateLimitingLockout() {
        // First 4 failed attempts
        for (int i = 0; i < 4; i++) {
            AuthService.AuthResult res = authService.authenticate("student1", "WrongPass".toCharArray());
            assertFalse(res.isSuccess());
            assertFalse(authService.isLockedOut(), "Should not be locked out before 5 failed attempts");
        }

        // 5th failed attempt: triggers lockout
        AuthService.AuthResult res5 = authService.authenticate("student1", "WrongPass".toCharArray());
        assertFalse(res5.isSuccess());
        assertTrue(authService.isLockedOut(), "Should be locked out on 5th failed attempt");
        assertTrue(authService.getLockoutRemainingSeconds() > 0, "Remaining seconds should be positive");

        // 6th attempt while locked should return locked result immediately
        AuthService.AuthResult res6 = authService.authenticate("student1", AppConfig.DEMO_STUDENT1_PASS.toCharArray());
        assertFalse(res6.isSuccess(), "Attempt while locked out must be blocked even with correct password");
        assertTrue(res6.getErrorMessage().contains("Account temporarily locked"));

        // Reset lockout
        authService.resetLockout();
        assertFalse(authService.isLockedOut());
        AuthService.AuthResult resAfterReset = authService.authenticate("student1", AppConfig.DEMO_STUDENT1_PASS.toCharArray());
        assertTrue(resAfterReset.isSuccess(), "Login should succeed after lockout reset");
    }
}
