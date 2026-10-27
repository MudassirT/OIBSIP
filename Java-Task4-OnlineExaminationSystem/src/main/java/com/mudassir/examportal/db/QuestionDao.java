package com.mudassir.examportal.db;

import com.mudassir.examportal.model.Question;

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
 * Data Access Object for Question entities.
 */
public class QuestionDao {

    private static final Logger LOGGER = Logger.getLogger(QuestionDao.class.getName());

    public static final String TABLE_NAME = "questions";
    public static final String COL_ID = "id";
    public static final String COL_CATEGORY = "category";
    public static final String COL_QUESTION_TEXT = "question_text";
    public static final String COL_OPTION_A = "option_a";
    public static final String COL_OPTION_B = "option_b";
    public static final String COL_OPTION_C = "option_c";
    public static final String COL_OPTION_D = "option_d";
    public static final String COL_CORRECT_OPTION = "correct_option";

    private final DatabaseManager databaseManager;

    public QuestionDao(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    /**
     * Retrieves all questions currently in the question bank.
     *
     * @return unmodifiable list of questions
     */
    public List<Question> findAll() {
        String sql = "SELECT " + COL_ID + ", " + COL_CATEGORY + ", " + COL_QUESTION_TEXT + ", "
                + COL_OPTION_A + ", " + COL_OPTION_B + ", " + COL_OPTION_C + ", " + COL_OPTION_D + ", "
                + COL_CORRECT_OPTION + " FROM " + TABLE_NAME + " ORDER BY " + COL_ID;

        List<Question> questions = new ArrayList<>();
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                questions.add(mapRowToQuestion(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error fetching all questions", e);
        }
        return Collections.unmodifiableList(questions);
    }

    /**
     * Counts the total number of questions in the database.
     *
     * @return count of questions
     */
    public int count() {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME;
        try (Connection conn = databaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error counting questions", e);
        }
        return 0;
    }

    /**
     * Inserts a new question into the database.
     *
     * @param question question object
     * @return generated ID or -1 on failure
     */
    public int insert(Question question) {
        String sql = "INSERT INTO " + TABLE_NAME + " (" + COL_CATEGORY + ", " + COL_QUESTION_TEXT + ", "
                + COL_OPTION_A + ", " + COL_OPTION_B + ", " + COL_OPTION_C + ", " + COL_OPTION_D + ", "
                + COL_CORRECT_OPTION + ") VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, question.getCategory());
            stmt.setString(2, question.getQuestionText());
            stmt.setString(3, question.getOptionA());
            stmt.setString(4, question.getOptionB());
            stmt.setString(5, question.getOptionC());
            stmt.setString(6, question.getOptionD());
            stmt.setString(7, question.getCorrectOption());

            stmt.executeUpdate();
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    question.setId(generatedId);
                    return generatedId;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error inserting question", e);
        }
        return -1;
    }

    private Question mapRowToQuestion(ResultSet rs) throws SQLException {
        return new Question(
                rs.getInt(COL_ID),
                rs.getString(COL_CATEGORY),
                rs.getString(COL_QUESTION_TEXT),
                rs.getString(COL_OPTION_A),
                rs.getString(COL_OPTION_B),
                rs.getString(COL_OPTION_C),
                rs.getString(COL_OPTION_D),
                rs.getString(COL_CORRECT_OPTION)
        );
    }
}
