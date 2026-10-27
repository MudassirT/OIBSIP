package com.mudassir.examportal;

import com.mudassir.examportal.db.AttemptDao;
import com.mudassir.examportal.db.DatabaseManager;
import com.mudassir.examportal.db.QuestionDao;
import com.mudassir.examportal.model.ExamResult;
import com.mudassir.examportal.model.ExamStatus;
import com.mudassir.examportal.model.Question;
import com.mudassir.examportal.model.User;
import com.mudassir.examportal.service.ExamService;
import com.mudassir.examportal.service.ExamSession;
import com.mudassir.examportal.ui.ExamPanel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ExamPanel Reattempt & Button State Lifecycle Tests")
class ExamPanelReattemptTest {

    private ExamService examService;
    private User testUser;
    private List<Question> sampleQuestions;

    @BeforeEach
    void setUp() throws Exception {
        File tempDb = File.createTempFile("exam_panel_test_", ".db");
        tempDb.deleteOnExit();
        DatabaseManager dbManager = new DatabaseManager("jdbc:sqlite:" + tempDb.getAbsolutePath());
        dbManager.initialize();

        QuestionDao questionDao = new QuestionDao(dbManager);
        AttemptDao attemptDao = new AttemptDao(dbManager);
        examService = new ExamService(questionDao, attemptDao);

        testUser = new User(1, "student1", "salt", "hash", "Student One");

        sampleQuestions = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            sampleQuestions.add(new Question(
                    i, "OOP", "Question #" + i, "Opt A", "Opt B", "Opt C", "Opt D", "A"
            ));
        }
    }

    @Test
    @DisplayName("Submit button and controls must be re-enabled when student reattempts the quiz")
    void testSubmitButtonReEnabledOnReattempt() {
        AtomicReference<ExamResult> submittedResult = new AtomicReference<>();
        ExamPanel panel = new ExamPanel(examService, submittedResult::set);

        // 1. First Exam Attempt
        ExamSession session1 = new ExamSession(sampleQuestions, 1800);
        panel.startExam(testUser, session1);

        assertTrue(panel.getSubmitButton().isEnabled(), "Submit button must be enabled initially");
        assertTrue(panel.getClearButton().isEnabled(), "Clear button must be enabled initially");
        if (panel.getNavigatorPanel().getNavigatorSubmitButton() != null) {
            assertTrue(panel.getNavigatorPanel().getNavigatorSubmitButton().isEnabled(),
                    "Navigator submit button must be enabled initially");
        }

        // Answer a question and submit
        session1.selectCurrentAnswer("A");
        panel.loadCurrentQuestion();

        // Simulate auto/manual submission
        panel.handleAutoSubmit();

        // 2. Second Exam Attempt (Reattempt)
        ExamSession session2 = new ExamSession(sampleQuestions, 1800);
        panel.startExam(testUser, session2);

        // Verify that after reattempt, Submit Exam is clickable and enabled
        assertTrue(panel.getSubmitButton().isEnabled(),
                "Submit button MUST be enabled and blue upon reattempting the quiz");
        assertTrue(panel.getClearButton().isEnabled(),
                "Clear button MUST be enabled upon reattempting the quiz");
        assertTrue(panel.getMarkForReviewCheckbox().isEnabled(),
                "Mark for review checkbox MUST be enabled upon reattempting the quiz");

        if (panel.getNavigatorPanel().getNavigatorSubmitButton() != null) {
            assertTrue(panel.getNavigatorPanel().getNavigatorSubmitButton().isEnabled(),
                    "Navigator submit button MUST be enabled upon reattempting the quiz");
        }
    }
}
