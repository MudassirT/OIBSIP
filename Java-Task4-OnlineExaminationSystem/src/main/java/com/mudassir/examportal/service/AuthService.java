package com.mudassir.examportal.service;

import com.mudassir.examportal.config.AppConfig;
import com.mudassir.examportal.db.UserDao;
import com.mudassir.examportal.model.User;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Service handling user authentication, PBKDF2 verification, and rate limiting lockout.
 */
public class AuthService {

    private static final Logger LOGGER = Logger.getLogger(AuthService.class.getName());

    private final UserDao userDao;
    private final AtomicInteger failedAttempts = new AtomicInteger(0);
    private volatile Instant lockoutUntil = null;

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * Result of an authentication attempt.
     */
    public static class AuthResult {
        private final boolean success;
        private final String errorMessage;
        private final User user;
        private final long lockoutRemainingSeconds;

        private AuthResult(boolean success, String errorMessage, User user, long lockoutRemainingSeconds) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.user = user;
            this.lockoutRemainingSeconds = lockoutRemainingSeconds;
        }

        public static AuthResult success(User user) {
            return new AuthResult(true, null, user, 0);
        }

        public static AuthResult failure(String errorMessage) {
            return new AuthResult(false, errorMessage, null, 0);
        }

        public static AuthResult locked(long remainingSeconds) {
            return new AuthResult(false, "Account temporarily locked due to 5 failed attempts. Please wait "
                    + remainingSeconds + " second(s) before trying again.", null, remainingSeconds);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public User getUser() {
            return user;
        }

        public long getLockoutRemainingSeconds() {
            return lockoutRemainingSeconds;
        }
    }

    /**
     * Authenticates a user given their username and password.
     *
     * @param username student username
     * @param password student password as char array
     * @return AuthResult indicating success or failure details
     */
    public synchronized AuthResult authenticate(String username, char[] password) {
        // Check rate limiting lockout
        if (isLockedOut()) {
            return AuthResult.locked(getLockoutRemainingSeconds());
        }

        // Validate non-empty inputs
        if (!ValidationUtil.isNotEmpty(username) || !ValidationUtil.isNotEmpty(password)) {
            return AuthResult.failure("Please enter both username and password.");
        }

        String trimmedUser = username.trim();
        Optional<User> userOpt = userDao.findByUsername(trimmedUser);

        if (userOpt.isEmpty()) {
            recordFailedAttempt();
            return AuthResult.failure("Invalid username or password.");
        }

        User user = userOpt.get();
        boolean passwordMatches = PasswordUtil.verifyPassword(password, user.getSalt(), user.getPasswordHash());

        if (!passwordMatches) {
            recordFailedAttempt();
            return AuthResult.failure("Invalid username or password.");
        }

        // Authentication successful: reset rate limiter
        failedAttempts.set(0);
        lockoutUntil = null;
        LOGGER.info("Successful login for user: " + user.getUsername());
        return AuthResult.success(user);
    }

    private void recordFailedAttempt() {
        int current = failedAttempts.incrementAndGet();
        LOGGER.warning("Failed login attempt " + current + " of " + AppConfig.MAX_FAILED_LOGIN_ATTEMPTS);
        if (current >= AppConfig.MAX_FAILED_LOGIN_ATTEMPTS) {
            lockoutUntil = Instant.now().plusSeconds(AppConfig.LOCKOUT_DURATION_SECONDS);
            LOGGER.warning("Maximum failed login attempts reached. Form locked for "
                    + AppConfig.LOCKOUT_DURATION_SECONDS + " seconds.");
        }
    }

    public synchronized boolean isLockedOut() {
        if (lockoutUntil == null) return false;
        if (Instant.now().isBefore(lockoutUntil)) {
            return true;
        }
        // Lockout expired: reset
        lockoutUntil = null;
        failedAttempts.set(0);
        return false;
    }

    public synchronized long getLockoutRemainingSeconds() {
        if (lockoutUntil == null) return 0;
        long diff = java.time.Duration.between(Instant.now(), lockoutUntil).getSeconds();
        return Math.max(0, diff);
    }

    public synchronized void resetLockout() {
        failedAttempts.set(0);
        lockoutUntil = null;
    }

    public int getFailedAttemptsCount() {
        return failedAttempts.get();
    }
}
