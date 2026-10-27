package com.mudassir.examportal;

import com.mudassir.examportal.config.AppConfig;
import com.mudassir.examportal.db.AttemptDao;
import com.mudassir.examportal.db.DatabaseManager;
import com.mudassir.examportal.db.QuestionDao;
import com.mudassir.examportal.db.UserDao;
import com.mudassir.examportal.model.ExamAttempt;
import com.mudassir.examportal.model.ExamStatus;
import com.mudassir.examportal.model.User;
import com.mudassir.examportal.service.AuthService;
import com.mudassir.examportal.service.ExamService;
import com.mudassir.examportal.service.ProfileService;
import com.mudassir.examportal.ui.MainFrame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MainFrame Window Close & Quit Exam Lifecycle Tests")
class MainFrameCloseAndQuitTest {

    private AuthService authService;
    private ProfileService profileService;
    private ExamService examService;
    private User testUser;
    private MainFrame mainFrame;

    @BeforeEach
    void setUp() throws Exception {
        File tempDb = File.createTempFile("mainframe_close_test_", ".db");
        tempDb.deleteOnExit();
        DatabaseManager dbManager = new DatabaseManager("jdbc:sqlite:" + tempDb.getAbsolutePath());
        dbManager.initialize();

        UserDao userDao = new UserDao(dbManager);
        QuestionDao questionDao = new QuestionDao(dbManager);
        AttemptDao attemptDao = new AttemptDao(dbManager);

        authService = new AuthService(userDao);
        profileService = new ProfileService(userDao);
        examService = new ExamService(questionDao, attemptDao);

        testUser = userDao.findByUsername("student1").orElseThrow();

        // Run Swing initialization on EDT
        SwingUtilities.invokeAndWait(() -> {
            mainFrame = new MainFrame(authService, profileService, examService);
        });
    }

    @Test
    @DisplayName("Quitting an active exam records attempt as ABANDONED and returns user to profile dashboard")
    void testQuitExamAndReturnToProfile() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // Log in and start exam
            mainFrame.showProfile(testUser);
            mainFrame.startExam();
        });

        assertEquals(AppConfig.CARD_EXAM, mainFrame.getCurrentCard());
        assertNotNull(mainFrame.getActiveExamSession());
        assertFalse(mainFrame.getActiveExamSession().isSubmitted());

        // Execute quit exam without modal prompt
        SwingUtilities.invokeAndWait(() -> {
            mainFrame.quitExamAndReturnToProfile(false);
        });

        // Verify session state and navigation
        assertTrue(mainFrame.getActiveExamSession().isSubmitted());
        assertEquals(AppConfig.CARD_PROFILE, mainFrame.getCurrentCard());
        assertFalse(mainFrame.getHeaderPanel().isTimerVisible());

        // Verify attempt was persisted into DB as ABANDONED
        List<ExamAttempt> history = examService.getPreviousAttempts(testUser.getId());
        assertFalse(history.isEmpty());
        assertEquals(ExamStatus.ABANDONED, history.get(history.size() - 1).getStatus());

        // Clean up
        SwingUtilities.invokeAndWait(() -> mainFrame.dispose());
    }

    @Test
    @DisplayName("Starting exam again after quitting successfully provides a fresh and fully interactive exam")
    void testStartExamAfterQuitting() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            mainFrame.showProfile(testUser);
            mainFrame.startExam();
            mainFrame.quitExamAndReturnToProfile(false);

            // Re-attempt exam
            mainFrame.startExam();
        });

        assertEquals(AppConfig.CARD_EXAM, mainFrame.getCurrentCard());
        assertNotNull(mainFrame.getActiveExamSession());
        assertFalse(mainFrame.getActiveExamSession().isSubmitted());
        assertTrue(mainFrame.getHeaderPanel().isTimerVisible());

        // Clean up
        SwingUtilities.invokeAndWait(() -> mainFrame.dispose());
    }
}
