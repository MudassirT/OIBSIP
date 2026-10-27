package com.mudassir.examportal.db;

import com.mudassir.examportal.config.AppConfig;
import com.mudassir.examportal.model.User;
import com.mudassir.examportal.service.PasswordUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages SQLite database connectivity, schema creation, and automatic seed data population.
 */
public class DatabaseManager {

    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());

    private final String jdbcUrl;

    public DatabaseManager() {
        this(AppConfig.getDatabaseUrl());
    }

    public DatabaseManager(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    /**
     * Obtains a new database connection with foreign keys enabled.
     *
     * @return open java.sql.Connection
     * @throws SQLException if a database access error occurs
     */
    public Connection getConnection() throws SQLException {
        // Ensure data directory exists if using file-based SQLite
        if (jdbcUrl.startsWith(AppConfig.DB_JDBC_PREFIX) && !jdbcUrl.contains(":memory:")) {
            String filePath = jdbcUrl.substring(AppConfig.DB_JDBC_PREFIX.length());
            File dbFile = new File(filePath);
            File parentDir = dbFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
        }

        Connection conn = DriverManager.getConnection(jdbcUrl);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    /**
     * Initializes the database: creates schema tables and seeds default users and questions.
     */
    public void initialize() {
        try {
            createSchema();
            seedUsersIfEmpty();
            seedQuestionsIfEmpty();
            LOGGER.info("Database initialized successfully at: " + jdbcUrl);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize database schema or seed data", e);
            throw new RuntimeException("Database initialization failure", e);
        }
    }

    /**
     * Executes the schema.sql DDL script.
     */
    private void createSchema() throws SQLException {
        String schemaSql = loadResourceAsString("/schema.sql");
        if (schemaSql == null || schemaSql.trim().isEmpty()) {
            throw new IllegalStateException("schema.sql resource not found or empty");
        }

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            // Split by semicolon for individual DDL statements
            String[] statements = schemaSql.split(";");
            for (String sql : statements) {
                String trimmed = sql.trim();
                if (!trimmed.isEmpty()) {
                    stmt.execute(trimmed);
                }
            }
        }
    }

    /**
     * Seeds the two demo student accounts if the users table is currently empty.
     */
    private void seedUsersIfEmpty() throws SQLException {
        String countSql = "SELECT COUNT(*) FROM users";
        try (Connection conn = getConnection();
             PreparedStatement countStmt = conn.prepareStatement(countSql);
             ResultSet rs = countStmt.executeQuery()) {
            if (rs.next() && rs.getInt(1) == 0) {
                LOGGER.info("Seeding initial student demo accounts...");

                // Demo Student 1: student1 / Student@123
                String salt1 = PasswordUtil.generateSalt();
                String hash1 = PasswordUtil.hashPassword(AppConfig.DEMO_STUDENT1_PASS.toCharArray(), salt1);
                insertUserDirect(conn, AppConfig.DEMO_STUDENT1_USER, AppConfig.DEMO_STUDENT1_NAME, hash1, salt1);

                // Demo Student 2: student2 / Student@456
                String salt2 = PasswordUtil.generateSalt();
                String hash2 = PasswordUtil.hashPassword(AppConfig.DEMO_STUDENT2_PASS.toCharArray(), salt2);
                insertUserDirect(conn, AppConfig.DEMO_STUDENT2_USER, AppConfig.DEMO_STUDENT2_NAME, hash2, salt2);

                LOGGER.info("Demo student accounts seeded successfully.");
            }
        }
    }

    private void insertUserDirect(Connection conn, String username, String displayName, String passwordHash, String salt) throws SQLException {
        String insertSql = "INSERT INTO users (username, display_name, password_hash, salt) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
            stmt.setString(1, username);
            stmt.setString(2, displayName);
            stmt.setString(3, passwordHash);
            stmt.setString(4, salt);
            stmt.executeUpdate();
        }
    }

    /**
     * Seeds the question bank if the questions table is currently empty.
     */
    private void seedQuestionsIfEmpty() throws SQLException {
        String countSql = "SELECT COUNT(*) FROM questions";
        try (Connection conn = getConnection();
             PreparedStatement countStmt = conn.prepareStatement(countSql);
             ResultSet rs = countStmt.executeQuery()) {
            if (rs.next() && rs.getInt(1) == 0) {
                LOGGER.info("Seeding question bank from seed_questions.sql...");
                String seedSql = loadResourceAsString("/seed_questions.sql");
                if (seedSql != null && !seedSql.trim().isEmpty()) {
                    try (Statement stmt = conn.createStatement()) {
                        stmt.executeUpdate(seedSql.trim());
                    }
                }
                LOGGER.info("Question bank seeded successfully.");
            }
        }
    }

    /**
     * Helper to load classpath resource files safely as UTF-8 strings.
     */
    private String loadResourceAsString(String resourcePath) {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                LOGGER.log(Level.SEVERE, "Resource not found: " + resourcePath);
                return null;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                return sb.toString();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading resource: " + resourcePath, e);
            return null;
        }
    }
}
