package com.mudassir.examportal.db;

import com.mudassir.examportal.model.ExamAttempt;
import com.mudassir.examportal.model.ExamStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for ExamAttempt records.
 */
public class AttemptDao {

    private static final Logger LOGGER = Logger.getLogger(AttemptDao.class.getName());

    public static final String TABLE_NAME = "exam_attempts";
    public static final String COL_ID = "id";
    public static final String COL_USER_ID = "user_id";
    public static final String COL_STARTED_AT = "started_at";
    public static final String COL_ENDED_AT = "ended_at";
    public static final String COL_SCORE = "score";
    public static final String COL_TOTAL_QUESTIONS = "total_questions";
    public static final String COL_STATUS = "status";

    private final DatabaseManager databaseManager;

    public AttemptDao(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    /**
     * Inserts an exam attempt record.
     *
     * @param attempt attempt to persist
     * @return generated ID or -1 on failure
     */
    public int insertAttempt(ExamAttempt attempt) {
        if (attempt == null) return -1;
        String sql = "INSERT INTO " + TABLE_NAME + " (" + COL_USER_ID + ", " + COL_STARTED_AT + ", "
                + COL_ENDED_AT + ", " + COL_SCORE + ", " + COL_TOTAL_QUESTIONS + ", " + COL_STATUS
                + ") VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, attempt.getUserId());
            stmt.setString(2, attempt.getStartedAt());
            stmt.setString(3, attempt.getEndedAt());
            stmt.setInt(4, attempt.getScore());
            stmt.setInt(5, attempt.getTotalQuestions());
            stmt.setString(6, attempt.getStatus() != null ? attempt.getStatus().name() : ExamStatus.SUBMITTED.name());

            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int generatedId = keys.getInt(1);
                    attempt.setId(generatedId);
                    return generatedId;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error inserting exam attempt", e);
        }
        return -1;
    }

    /**
     * Retrieves all exam attempts made by a specific user, ordered from most recent to oldest.
     *
     * @param userId user ID
     * @return unmodifiable list of attempts
     */
    public List<ExamAttempt> findAttemptsByUserId(int userId) {
        String sql = "SELECT " + COL_ID + ", " + COL_USER_ID + ", " + COL_STARTED_AT + ", "
                + COL_ENDED_AT + ", " + COL_SCORE + ", " + COL_TOTAL_QUESTIONS + ", " + COL_STATUS
                + " FROM " + TABLE_NAME + " WHERE " + COL_USER_ID + " = ? ORDER BY " + COL_ID + " DESC";

        List<ExamAttempt> list = new ArrayList<>();
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToAttempt(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error finding attempts for user ID: " + userId, e);
        }
        return Collections.unmodifiableList(list);
    }

    private ExamAttempt mapRowToAttempt(ResultSet rs) throws SQLException {
        return new ExamAttempt(
                rs.getInt(COL_ID),
                rs.getInt(COL_USER_ID),
                rs.getString(COL_STARTED_AT),
                rs.getString(COL_ENDED_AT),
                rs.getInt(COL_SCORE),
                rs.getInt(COL_TOTAL_QUESTIONS),
                ExamStatus.fromString(rs.getString(COL_STATUS))
        );
    }
}
