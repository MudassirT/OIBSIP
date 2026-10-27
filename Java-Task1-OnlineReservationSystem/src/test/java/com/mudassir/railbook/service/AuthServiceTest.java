package com.mudassir.railbook.service;

import com.mudassir.railbook.db.DatabaseManager;
import com.mudassir.railbook.db.UserDao;
import com.mudassir.railbook.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for authentication logic, password verification, lockout handling, and registration.
 *
 * @author Mudassir Ahmed
 */
class AuthServiceTest {

    private static final String TEST_DB_PATH = "target/test_auth.db";
    private DatabaseManager dbManager;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        File dbFile = new File(TEST_DB_PATH);
        if (dbFile.exists()) {
            dbFile.delete();
        }
        dbManager = new DatabaseManager("jdbc:sqlite:" + TEST_DB_PATH);
        dbManager.initializeDatabase();

        UserDao userDao = new UserDao(dbManager);
        authService = new AuthService(userDao);
        authService.resetAllLockouts();
    }

    @AfterEach
    void tearDown() {
        File dbFile = new File(TEST_DB_PATH);
        if (dbFile.exists()) {
            dbFile.delete();
        }
    }

    @Test
    @DisplayName("Demo users (admin and demo) log in successfully with seeded credentials")
    void testDemoUsersLoginSuccess() throws AuthenticationException {
        User admin = authService.login("admin", "Admin@123");
        assertNotNull(admin);
        assertEquals("admin", admin.getUsername());

        User demo = authService.login("demo", "Demo@123");
        assertNotNull(demo);
        assertEquals("demo", demo.getUsername());
    }

    @Test
    @DisplayName("Invalid password triggers AuthenticationException with generic message")
    void testInvalidPasswordFailure() {
        AuthenticationException ex = assertThrows(AuthenticationException.class, () ->
                authService.login("admin", "WrongPassword!123"));

        assertEquals("Invalid username or password. Please try again.", ex.getMessage(),
                "Error message must not reveal that the username was valid");
    }

    @Test
    @DisplayName("Non-existent user triggers AuthenticationException with identical generic message")
    void testUnknownUserFailure() {
        AuthenticationException ex = assertThrows(AuthenticationException.class, () ->
                authService.login("non_existent_user", "SomePass@123"));

        assertEquals("Invalid username or password. Please try again.", ex.getMessage(),
                "Error message must not reveal that the username does not exist");
    }

    @Test
    @DisplayName("Empty username or password throws AuthenticationException immediately")
    void testEmptyCredentials() {
        assertThrows(AuthenticationException.class, () -> authService.login("", "Admin@123"));
        assertThrows(AuthenticationException.class, () -> authService.login("   ", "Admin@123"));
        assertThrows(AuthenticationException.class, () -> authService.login("admin", ""));
        assertThrows(AuthenticationException.class, () -> authService.login(null, "Admin@123"));
    }

    @Test
    @DisplayName("5 consecutive failed login attempts lock the account for 30 seconds")
    void testAccountLockoutAfter5Attempts() {
        // First 4 failed attempts throw regular AuthenticationException
        for (int i = 1; i <= 4; i++) {
            final int attempt = i;
            assertThrows(AuthenticationException.class, () ->
                    authService.login("admin", "WrongPassword_" + attempt));
        }

        // 5th failed attempt triggers AccountLockedException
        AccountLockedException lockedEx = assertThrows(AccountLockedException.class, () ->
                authService.login("admin", "WrongPassword_5"));

        assertTrue(lockedEx.getRemainingSeconds() > 0, "Remaining seconds should be positive");
        assertTrue(lockedEx.getRemainingSeconds() <= 30, "Lockout duration should be <= 30 seconds");

        // Subsequent attempts within lockout window also fail with AccountLockedException
        assertThrows(AccountLockedException.class, () ->
                authService.login("admin", "Admin@123"));
    }

    @Test
    @DisplayName("User registration hashes password and allows subsequent login")
    void testUserRegistrationAndLogin() throws Exception {
        User newUser = authService.register("newuser", "SecurePass@99");
        assertNotNull(newUser);
        assertTrue(newUser.getId() > 0);

        User loggedIn = authService.login("newuser", "SecurePass@99");
        assertNotNull(loggedIn);
        assertEquals("newuser", loggedIn.getUsername());
    }
}
