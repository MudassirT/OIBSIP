package com.mudassir.railbook.db;

import com.mudassir.railbook.config.Constants;
import com.mudassir.railbook.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for User entities.
 * Ensures parameterized queries and try-with-resources resource management.
 *
 * @author Mudassir Ahmed
 */
public class UserDao {

    private static final Logger LOGGER = Logger.getLogger(UserDao.class.getName());
    private final DatabaseManager databaseManager;

    public UserDao(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    /**
     * Finds a user by their unique username.
     *
     * @param username user's username
     * @return Optional containing the user if found, or empty Optional
     */
    public Optional<User> findByUsername(String username) {
        if (username == null) {
            return Optional.empty();
        }
        String sql = "SELECT " + Constants.COL_USER_ID + ", " +
                Constants.COL_USER_USERNAME + ", " +
                Constants.COL_USER_PASSWORD_HASH + ", " +
                Constants.COL_USER_SALT +
                " FROM " + Constants.TABLE_USERS +
                " WHERE " + Constants.COL_USER_USERNAME + " = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User(
                            rs.getInt(Constants.COL_USER_ID),
                            rs.getString(Constants.COL_USER_USERNAME),
                            rs.getString(Constants.COL_USER_PASSWORD_HASH),
                            rs.getString(Constants.COL_USER_SALT)
                    );
                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error querying user by username: " + username, e);
        }
        return Optional.empty();
    }

    /**
     * Finds a user by their primary key ID.
     *
     * @param id user ID
     * @return Optional containing the user if found, or empty Optional
     */
    public Optional<User> findById(int id) {
        String sql = "SELECT " + Constants.COL_USER_ID + ", " +
                Constants.COL_USER_USERNAME + ", " +
                Constants.COL_USER_PASSWORD_HASH + ", " +
                Constants.COL_USER_SALT +
                " FROM " + Constants.TABLE_USERS +
                " WHERE " + Constants.COL_USER_ID + " = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User(
                            rs.getInt(Constants.COL_USER_ID),
                            rs.getString(Constants.COL_USER_USERNAME),
                            rs.getString(Constants.COL_USER_PASSWORD_HASH),
                            rs.getString(Constants.COL_USER_SALT)
                    );
                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error querying user by id: " + id, e);
        }
        return Optional.empty();
    }

    /**
     * Persists a new user in the database.
     *
     * @param user user entity to save
     * @return saved user with generated ID
     * @throws SQLException if a database error occurs or username already exists
     */
    public User save(User user) throws SQLException {
        String sql = "INSERT INTO " + Constants.TABLE_USERS + " (" +
                Constants.COL_USER_USERNAME + ", " +
                Constants.COL_USER_PASSWORD_HASH + ", " +
                Constants.COL_USER_SALT + ") VALUES (?, ?, ?)";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getSalt());
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                }
            }
            return user;
        }
    }

    /**
     * Returns total count of users in database.
     *
     * @return user count
     */
    public int count() {
        String sql = "SELECT COUNT(*) FROM " + Constants.TABLE_USERS;
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error counting users", e);
        }
        return 0;
    }
}
