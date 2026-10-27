package com.mudassir.examportal.service;

import com.mudassir.examportal.db.UserDao;
import com.mudassir.examportal.model.User;

import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Service managing user profile updates (display name and password modifications).
 */
public class ProfileService {

    private static final Logger LOGGER = Logger.getLogger(ProfileService.class.getName());

    private final UserDao userDao;

    public ProfileService(UserDao userDao) {
        this.userDao = userDao;
    }

    public static class ProfileResult {
        private final boolean success;
        private final String message;

        public ProfileResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static ProfileResult ok(String message) {
            return new ProfileResult(true, message);
        }

        public static ProfileResult error(String message) {
            return new ProfileResult(false, message);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * Updates the user's display name with validation.
     *
     * @param user           active logged-in user
     * @param newDisplayName new display name to apply
     * @return ProfileResult indicating success or validation error
     */
    public ProfileResult updateDisplayName(User user, String newDisplayName) {
        if (user == null) {
            return ProfileResult.error("User session is invalid.");
        }
        if (newDisplayName == null || !ValidationUtil.isValidDisplayName(newDisplayName)) {
            return ProfileResult.error("Display name must be between 2 and 40 characters and contain only letters, spaces, dots, and hyphens.");
        }

        String trimmed = newDisplayName.trim();
        if (trimmed.equals(user.getDisplayName())) {
            return ProfileResult.ok("Display name is already up to date.");
        }

        boolean updated = userDao.updateDisplayName(user.getId(), trimmed);
        if (updated) {
            user.setDisplayName(trimmed);
            LOGGER.info("Display name updated successfully for user ID: " + user.getId());
            return ProfileResult.ok("Display name updated successfully.");
        } else {
            return ProfileResult.error("Failed to update display name in the database.");
        }
    }

    /**
     * Updates the user's password after validating current password, new password complexity, and confirmation.
     *
     * @param user            active logged-in user
     * @param currentPassword supplied current password
     * @param newPassword     supplied new password
     * @param confirmPassword confirmation of new password
     * @return ProfileResult indicating success or validation error
     */
    public ProfileResult updatePassword(User user, char[] currentPassword, char[] newPassword, char[] confirmPassword) {
        if (user == null) {
            return ProfileResult.error("User session is invalid.");
        }

        // Validate current password provided
        if (!ValidationUtil.isNotEmpty(currentPassword)) {
            return ProfileResult.error("Please enter your current password.");
        }

        // Verify current password matches
        boolean currentValid = PasswordUtil.verifyPassword(currentPassword, user.getSalt(), user.getPasswordHash());
        if (!currentValid) {
            return ProfileResult.error("Current password is incorrect.");
        }

        // Validate new password provided
        if (!ValidationUtil.isNotEmpty(newPassword)) {
            return ProfileResult.error("Please enter a new password.");
        }

        // Validate new password complexity (minimum 8 chars, at least one letter and one number)
        if (!ValidationUtil.isValidNewPassword(newPassword)) {
            return ProfileResult.error("New password must be at least 8 characters long and contain at least one letter and one number.");
        }

        // Validate confirmation matches
        if (confirmPassword == null || !Arrays.equals(newPassword, confirmPassword)) {
            return ProfileResult.error("New password and confirmation password do not match.");
        }

        // Hash new password with new random salt
        String newSalt = PasswordUtil.generateSalt();
        String newHash = PasswordUtil.hashPassword(newPassword, newSalt);

        boolean updated = userDao.updatePassword(user.getId(), newHash, newSalt);
        if (updated) {
            user.setPasswordHash(newHash);
            user.setSalt(newSalt);
            LOGGER.info("Password updated successfully for user ID: " + user.getId());
            return ProfileResult.ok("Password changed successfully.");
        } else {
            return ProfileResult.error("Failed to update password in the database.");
        }
    }
}
