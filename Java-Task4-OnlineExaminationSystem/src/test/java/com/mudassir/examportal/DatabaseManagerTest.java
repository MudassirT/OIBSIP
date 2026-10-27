package com.mudassir.examportal;

import com.mudassir.examportal.config.AppConfig;
import com.mudassir.examportal.db.DatabaseManager;
import com.mudassir.examportal.db.QuestionDao;
import com.mudassir.examportal.db.UserDao;
import com.mudassir.examportal.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Database Manager, Schema & Seed Data Tests")
class DatabaseManagerTest {

    private File tempDbFile;
    private DatabaseManager dbManager;

    @BeforeEach
    void setUp() throws Exception {
        tempDbFile = File.createTempFile("test_examportal_", ".db");
        tempDbFile.deleteOnExit();
        String jdbcUrl = "jdbc:sqlite:" + tempDbFile.getAbsolutePath();
        dbManager = new DatabaseManager(jdbcUrl);
        dbManager.initialize();
    }

    @AfterEach
    void tearDown() {
        if (tempDbFile != null && tempDbFile.exists()) {
            tempDbFile.delete();
        }
    }

    @Test
    @DisplayName("Should initialize schema and verify PRAGMA foreign_keys is enabled")
    void testForeignKeysEnabled() throws Exception {
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("PRAGMA foreign_keys;")) {
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1), "foreign_keys PRAGMA must be 1 (ON)");
        }
    }

    @Test
    @DisplayName("Should automatically seed demo student accounts")
    void testDemoStudentsSeeded() {
        UserDao userDao = new UserDao(dbManager);

        Optional<User> student1Opt = userDao.findByUsername(AppConfig.DEMO_STUDENT1_USER);
        assertTrue(student1Opt.isPresent(), "student1 demo account must be seeded");
        User student1 = student1Opt.get();
        assertEquals(AppConfig.DEMO_STUDENT1_NAME, student1.getDisplayName());
        assertNotNull(student1.getPasswordHash());
        assertNotNull(student1.getSalt());

        Optional<User> student2Opt = userDao.findByUsername(AppConfig.DEMO_STUDENT2_USER);
        assertTrue(student2Opt.isPresent(), "student2 demo account must be seeded");
    }

    @Test
    @DisplayName("Should seed question bank with at least 20 questions")
    void testQuestionBankSeeded() {
        QuestionDao questionDao = new QuestionDao(dbManager);
        int count = questionDao.count();
        assertTrue(count >= 20, "Question pool must contain at least 20 seeded questions, found: " + count);
    }
}
