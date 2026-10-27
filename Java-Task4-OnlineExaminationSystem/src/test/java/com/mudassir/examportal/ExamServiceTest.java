package com.mudassir.examportal;

import com.mudassir.examportal.config.AppConfig;
import com.mudassir.examportal.db.AttemptDao;
import com.mudassir.examportal.db.DatabaseManager;
import com.mudassir.examportal.db.QuestionDao;
import com.mudassir.examportal.db.UserDao;
import com.mudassir.examportal.model.ExamAttempt;
import com.mudassir.examportal.model.ExamResult;
import com.mudassir.examportal.model.ExamStatus;
import com.mudassir.examportal.model.Question;
import com.mudassir.examportal.model.User;
import com.mudassir.examportal.service.ExamService;
import com.mudassir.examportal.service.ExamSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Exam Service & Question Randomization Tests")
class ExamServiceTest {

    private File tempDb;
    private DatabaseManager dbManager;
    private QuestionDao questionDao;
    private AttemptDao attemptDao;
    private ExamService examService;
    private User testUser;

    @BeforeEach
    void setUp() throws Exception {
        tempDb = File.createTempFile("exam_service_test_", ".db");
        tempDb.deleteOnExit();
        dbManager = new DatabaseManager("jdbc:sqlite:" + tempDb.getAbsolutePath());
        dbManager.initialize();

        questionDao = new QuestionDao(dbManager);
        attemptDao = new AttemptDao(dbManager);
        examService = new ExamService(questionDao, attemptDao, new Random(42));

        UserDao userDao = new UserDao(dbManager);
        testUser = userDao.findByUsername(AppConfig.DEMO_STUDENT1_USER).orElseThrow();
    }

    @AfterEach
    void tearDown() {
        if (tempDb != null && tempDb.exists()) {
            tempDb.delete();
        }
    }

    @Test
    @DisplayName("Should select exactly 10 unique questions without duplicates")
    void testUniqueQuestionSelection() {
        ExamSession session = examService.createExamSession(60);
        List<Question> questions = session.getQuestions();

        assertEquals(AppConfig.QUESTIONS_PER_EXAM, questions.size(), "Must select exactly 10 questions");

        Set<Integer> uniqueIds = new HashSet<>();
        for (Question q : questions) {
            assertTrue(uniqueIds.add(q.getId()), "Question ID " + q.getId() + " must be unique in attempt");
        }
    }

    @Test
    @DisplayName("Option shuffling must keep the correct answer accurate across multiple runs")
    void testOptionShufflingPreservesCorrectAnswer() {
        Question original = new Question(
                101,
                "Java",
                "What is JVM?",
                "Java Virtual Machine",
                "Java Verified Memory",
                "Just Virtual Method",
                "Joint Vector Model",
                "A"
        );
        String expectedCorrectText = original.getCorrectOptionText(); // "Java Virtual Machine"

        // Perform 50 shuffles with different random seeds
        for (int seed = 0; seed < 50; seed++) {
            ExamService customService = new ExamService(questionDao, attemptDao, new Random(seed));
            Question shuffled = customService.randomizeQuestionOptions(original);

            // Shuffled options must contain the same 4 strings
            assertTrue(shuffled.getOptionsList().contains("Java Virtual Machine"));
            assertTrue(shuffled.getOptionsList().contains("Java Verified Memory"));
            assertTrue(shuffled.getOptionsList().contains("Just Virtual Method"));
            assertTrue(shuffled.getOptionsList().contains("Joint Vector Model"));

            // Correct answer text in the shuffled question MUST still match original correct answer text
            assertEquals(expectedCorrectText, shuffled.getCorrectOptionText(),
                    "Correct option text must be preserved after shuffling with seed " + seed);
        }
    }

    @Test
    @DisplayName("Should successfully persist attempts with statuses SUBMITTED, AUTO_SUBMITTED, and ABANDONED")
    void testRecordAttemptStatuses() {
        ExamSession session1 = examService.createExamSession(60);
        ExamResult resSubmitted = session1.submit(ExamStatus.SUBMITTED);
        int id1 = examService.recordAttempt(testUser, session1, resSubmitted);
        assertTrue(id1 > 0);

        ExamSession session2 = examService.createExamSession(60);
        ExamResult resAuto = session2.submit(ExamStatus.AUTO_SUBMITTED);
        int id2 = examService.recordAttempt(testUser, session2, resAuto);
        assertTrue(id2 > 0);

        ExamSession session3 = examService.createExamSession(60);
        ExamResult resAbandoned = session3.submit(ExamStatus.ABANDONED);
        int id3 = examService.recordAttempt(testUser, session3, resAbandoned);
        assertTrue(id3 > 0);

        // Fetch previous attempts
        List<ExamAttempt> attempts = examService.getPreviousAttempts(testUser.getId());
        assertEquals(3, attempts.size());
        assertEquals(ExamStatus.ABANDONED, attempts.get(0).getStatus());
        assertEquals(ExamStatus.AUTO_SUBMITTED, attempts.get(1).getStatus());
        assertEquals(ExamStatus.SUBMITTED, attempts.get(2).getStatus());
    }
}
