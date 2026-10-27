package com.mudassir.railbook.service;

import com.mudassir.railbook.config.Constants;
import com.mudassir.railbook.db.UserDao;
import com.mudassir.railbook.model.User;

import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Authentication service handling user login, lockout mechanics, session state,
 * and user registration.
 *
 * @author Mudassir Ahmed
 */
public class AuthService {

    private static final Logger LOGGER = Logger.getLogger(AuthService.class.getName());
    private static final String GENERIC_ERROR_MESSAGE = "Invalid username or password. Please try again.";

    private final UserDao userDao;
    private final Map<String, AttemptTracker> attemptMap = new ConcurrentHashMap<>();
    private final AttemptTracker globalTracker = new AttemptTracker();
    private User currentUser;

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * Authenticates a user with username and raw password.
     *
     * @param rawUsername entered username
     * @param rawPassword entered password
     * @return authenticated {@link User}
     * @throws AuthenticationException if credentials fail or input is empty
     * @throws AccountLockedException  if maximum failed attempts reached
     */
    public synchronized User login(String rawUsername, String rawPassword) throws AuthenticationException {
        // Block empty submissions
        if (rawUsername == null || rawUsername.trim().isEmpty() ||
                rawPassword == null || rawPassword.isEmpty()) {
            throw new AuthenticationException("Username and password cannot be empty.");
        }

        String username = rawUsername.trim();

        // Check global or username-specific lockout
        checkLockout(username);

        AttemptTracker tracker = attemptMap.computeIfAbsent(username.toLowerCase(), k -> new AttemptTracker());

        Optional<User> userOpt = userDao.findByUsername(username);

        if (userOpt.isEmpty()) {
            recordFailedAttempt(tracker);
            LOGGER.warning("Authentication failed: User '" + username + "' not found.");
            throw new AuthenticationException(GENERIC_ERROR_MESSAGE);
        }

        User user = userOpt.get();
        boolean verified = PasswordUtil.verifyPassword(rawPassword, user.getSalt(), user.getPasswordHash());

        if (!verified) {
            recordFailedAttempt(tracker);
            LOGGER.warning("Authentication failed: Invalid password for user '" + username + "'.");
            throw new AuthenticationException(GENERIC_ERROR_MESSAGE);
        }

        // Authentication successful
        tracker.reset();
        globalTracker.reset();
        this.currentUser = user;
        LOGGER.info("User '" + username + "' authenticated successfully.");
        return user;
    }

    /**
     * Checks if either the specific account or the client form is currently locked out.
     */
    private void checkLockout(String username) throws AccountLockedException {
        long now = System.currentTimeMillis();

        if (globalTracker.isLocked(now)) {
            long remaining = globalTracker.getRemainingSeconds(now);
            throw new AccountLockedException(remaining);
        }

        AttemptTracker tracker = attemptMap.get(username.toLowerCase());
        if (tracker != null && tracker.isLocked(now)) {
            long remaining = tracker.getRemainingSeconds(now);
            throw new AccountLockedException(remaining);
        }
    }

    private void recordFailedAttempt(AttemptTracker tracker) throws AccountLockedException {
        long now = System.currentTimeMillis();
        tracker.increment(now);
        globalTracker.increment(now);

        if (tracker.isLocked(now) || globalTracker.isLocked(now)) {
            long remaining = Math.max(tracker.getRemainingSeconds(now), globalTracker.getRemainingSeconds(now));
            LOGGER.warning("Form/Account locked due to 5 failed attempts for " + remaining + " seconds.");
            throw new AccountLockedException(remaining);
        }
    }

    /**
     * Registers a new user.
     *
     * @param username    unique username
     * @param rawPassword password to hash and store
     * @return newly created User
     * @throws ValidationException if username/password invalid
     * @throws SQLException        if database error or duplicate username
     */
    public synchronized User register(String username, String rawPassword) throws ValidationException, SQLException {
        if (username == null || username.trim().length() < 3) {
            throw new ValidationException("Username must be at least 3 characters.");
        }
        if (rawPassword == null || rawPassword.length() < 6) {
            throw new ValidationException("Password must be at least 6 characters.");
        }

        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(rawPassword, salt);
        User user = new User(username.trim(), hash, salt);
        return userDao.save(user);
    }

    /**
     * Retrieves the remaining lockout duration in seconds, if currently locked.
     *
     * @param username username to check
     * @return remaining seconds, or 0 if not locked
     */
    public long getRemainingLockoutSeconds(String username) {
        long now = System.currentTimeMillis();
        long globalRemaining = globalTracker.isLocked(now) ? globalTracker.getRemainingSeconds(now) : 0;
        if (username == null || username.isBlank()) {
            return globalRemaining;
        }
        AttemptTracker tracker = attemptMap.get(username.trim().toLowerCase());
        long userRemaining = (tracker != null && tracker.isLocked(now)) ? tracker.getRemainingSeconds(now) : 0;
        return Math.max(globalRemaining, userRemaining);
    }

    public synchronized void logout() {
        if (currentUser != null) {
            LOGGER.info("User '" + currentUser.getUsername() + "' logged out.");
            this.currentUser = null;
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /**
     * Resets lockout trackers (useful for unit testing).
     */
    public void resetAllLockouts() {
        attemptMap.clear();
        globalTracker.reset();
    }

    /**
     * Tracks failed attempts and lockout expiration timestamp.
     */
    private static class AttemptTracker {
        private int failedAttempts = 0;
        private long lockoutUntil = 0L;

        public synchronized void increment(long now) {
            failedAttempts++;
            if (failedAttempts >= Constants.MAX_FAILED_LOGIN_ATTEMPTS) {
                lockoutUntil = now + (Constants.LOCKOUT_DURATION_SECONDS * 1000L);
            }
        }

        public synchronized boolean isLocked(long now) {
            if (lockoutUntil > now) {
                return true;
            }
            if (lockoutUntil > 0 && lockoutUntil <= now) {
                // Lockout has expired, reset counter
                reset();
            }
            return false;
        }

        public synchronized long getRemainingSeconds(long now) {
            if (lockoutUntil <= now) {
                return 0;
            }
            return Math.max(1, (lockoutUntil - now + 999) / 1000);
        }

        public synchronized void reset() {
            failedAttempts = 0;
            lockoutUntil = 0L;
        }
    }
}
