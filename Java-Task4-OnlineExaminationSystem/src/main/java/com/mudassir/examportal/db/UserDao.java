package com.mudassir.examportal.db;

import com.mudassir.examportal.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for User entities.
 * Encapsulates all SQL queries using PreparedStatements with try-with-resources.
 */
public class UserDao {

    private static final Logger LOGGER = Logger.getLogger(UserDao.class.getName());

    // Table and column constants to eliminate magic strings
    public static final String TABLE_NAME = "users";
    public static final String COL_ID = "id";
    public static final String COL_USERNAME = "username";
    public static final String COL_DISPLAY_NAME = "display_name";
    public static final String COL_PASSWORD_HASH = "password_hash";
    public static final String COL_SALT = "salt";

    private final DatabaseManager databaseManager;

    public UserDao(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    /**
     * Finds a user by their unique username.
     *
     * @param username username to search for
     * @return Optional containing the User if found, or empty Optional
     */
    public Optional<User> findByUsername(String username) {
        if (username == null) return Optional.empty();
        String sql = "SELECT " + COL_ID + ", " + COL_USERNAME + ", " + COL_DISPLAY_NAME + ", "
                + COL_PASSWORD_HASH + ", " + COL_SALT + " FROM " + TABLE_NAME
                + " WHERE " + COL_USERNAME + " = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error finding user by username: " + username, e);
        }
        return Optional.empty();
    }

    /**
     * Finds a user by their primary key ID.
     *
     * @param id user ID
     * @return Optional containing User if found
     */
    public Optional<User> findById(int id) {
        String sql = "SELECT " + COL_ID + ", " + COL_USERNAME + ", " + COL_DISPLAY_NAME + ", "
                + COL_PASSWORD_HASH + ", " + COL_SALT + " FROM " + TABLE_NAME
                + " WHERE " + COL_ID + " = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error finding user by ID: " + id, e);
        }
        return Optional.empty();
    }

    /**
     * Updates a user's display name.
     *
     * @param userId         primary key
     * @param newDisplayName updated display name
     * @return true if updated successfully, false otherwise
     */
    public boolean updateDisplayName(int userId, String newDisplayName) {
        String sql = "UPDATE " + TABLE_NAME + " SET " + COL_DISPLAY_NAME + " = ? WHERE " + COL_ID + " = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newDisplayName);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error updating display name for user ID: " + userId, e);
            return false;
        }
    }

    /**
     * Updates a user's password hash and salt.
     *
     * @param userId          primary key
     * @param newPasswordHash new PBKDF2 hash
     * @param newSalt         new salt
     * @return true if updated successfully, false otherwise
     */
    public boolean updatePassword(int userId, String newPasswordHash, String newSalt) {
        String sql = "UPDATE " + TABLE_NAME + " SET " + COL_PASSWORD_HASH + " = ?, " + COL_SALT + " = ? WHERE " + COL_ID + " = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newPasswordHash);
            stmt.setString(2, newSalt);
            stmt.setInt(3, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error updating password for user ID: " + userId, e);
            return false;
        }
    }

    private User mapRowToUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt(COL_ID),
                rs.getString(COL_USERNAME),
                rs.getString(COL_DISPLAY_NAME),
                rs.getString(COL_PASSWORD_HASH),
                rs.getString(COL_SALT)
        );
    }
}
