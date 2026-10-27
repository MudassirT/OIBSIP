package com.mudassir.brainblitz;

import com.mudassir.brainblitz.data.Question;
import com.mudassir.brainblitz.data.QuestionRepository;
import com.mudassir.brainblitz.data.QuestionReview;
import com.mudassir.brainblitz.logic.QuizManager;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * JUnit test suite verifying the QuizManager core business logic,
 * question randomization, duplicate prevention, option shuffling,
 * score calculations, and defensive repository parsing.
 */
public class QuizLogicTest {

    private List<Question> samplePool;
    private QuizManager quizManager;

    @Before
    public void setUp() {
        quizManager = new QuizManager();
        samplePool = new ArrayList<>();

        for (int i = 1; i <= 20; i++) {
            samplePool.add(new Question(
                    i,
                    "Category " + (i % 5),
                    "Question text #" + i,
                    Arrays.asList("Option A" + i, "Option B" + i, "Option C" + i, "Option D" + i),
                    0,
                    "Explanation #" + i
            ));
        }
    }

    @Test
    public void testSelect10QuestionsFromPool() {
        quizManager.initRound(samplePool, 10);
        assertEquals(10, quizManager.getTotalQuestions());
        assertEquals(1, quizManager.getCurrentQuestionNumber());
        assertFalse(quizManager.isRoundOver());
    }

    @Test
    public void testNoDuplicateQuestionsInRound() {
        quizManager.initRound(samplePool, 10);
        List<Question> roundQuestions = quizManager.getRoundQuestions();

        Set<Integer> uniqueIds = new HashSet<>();
        for (Question q : roundQuestions) {
            assertTrue("Detected duplicate question ID: " + q.getId(), uniqueIds.add(q.getId()));
        }
        assertEquals(10, uniqueIds.size());
    }

    @Test
    public void testPoolSmallerThanRequestedCount() {
        List<Question> smallPool = samplePool.subList(0, 5);
        quizManager.initRound(smallPool, 10);

        assertEquals(5, quizManager.getTotalQuestions());
        assertEquals(5, quizManager.getRoundQuestions().size());
    }

    @Test
    public void testEmptyPoolHandling() {
        quizManager.initRound(new ArrayList<Question>(), 10);
        assertEquals(0, quizManager.getTotalQuestions());
        assertTrue(quizManager.isRoundOver());
    }

    @Test
    public void testOptionShufflingAndCorrectIndexRemapping() {
        Question original = new Question(
                100,
                "OS",
                "What is a Semaphore?",
                Arrays.asList("A signaling variable", "A CPU register", "A disk cache", "A bus"),
                0, // "A signaling variable"
                "Semaphores synchronize access to shared resources."
        );

        String correctText = original.getCorrectOptionText();
        assertEquals("A signaling variable", correctText);

        // Verify over several shuffles that the correct answer text is preserved and remapped
        for (int i = 0; i < 20; i++) {
            Question shuffled = original.createShuffledCopy();
            assertEquals(4, shuffled.getOptions().size());
            int newCorrectIndex = shuffled.getCorrectOptionIndex();
            assertTrue(newCorrectIndex >= 0 && newCorrectIndex < 4);
            assertEquals("Correct option text must match remapped index",
                    correctText, shuffled.getOptions().get(newCorrectIndex));
        }
    }

    @Test
    public void testCorrectAnswerCheckingAndScoring() {
        quizManager.initRound(samplePool, 10);
        Question current = quizManager.getCurrentQuestion();
        assertNotNull(current);

        int correctIndex = current.getCorrectOptionIndex();
        boolean isCorrect = quizManager.submitAnswer(correctIndex);

        assertTrue("Expected submitted answer to be correct", isCorrect);
        assertTrue(quizManager.isCurrentAnswered());
        assertEquals(1, quizManager.getScore());
        assertEquals(1, quizManager.getCorrectCount());
        assertEquals(0, quizManager.getIncorrectCount());
        assertEquals(1, quizManager.getReviewList().size());

        QuestionReview review = quizManager.getReviewList().get(0);
        assertTrue(review.isCorrect());
        assertFalse(review.isTimedOut());
        assertEquals(correctIndex, review.getSelectedOptionIndex());
    }

    @Test
    public void testIncorrectAnswerCheckingAndScoring() {
        quizManager.initRound(samplePool, 10);
        Question current = quizManager.getCurrentQuestion();
        assertNotNull(current);

        int wrongIndex = (current.getCorrectOptionIndex() + 1) % 4;
        boolean isCorrect = quizManager.submitAnswer(wrongIndex);

        assertFalse("Expected submitted answer to be incorrect", isCorrect);
        assertTrue(quizManager.isCurrentAnswered());
        assertEquals(0, quizManager.getScore());
        assertEquals(0, quizManager.getCorrectCount());
        assertEquals(1, quizManager.getIncorrectCount());

        QuestionReview review = quizManager.getReviewList().get(0);
        assertFalse(review.isCorrect());
        assertFalse(review.isTimedOut());
        assertEquals(wrongIndex, review.getSelectedOptionIndex());
    }

    @Test
    public void testTimeoutHandling() {
        quizManager.initRound(samplePool, 10);
        quizManager.submitTimeout();

        assertTrue(quizManager.isCurrentAnswered());
        assertEquals(-1, quizManager.getSelectedOptionIndex());
        assertEquals(0, quizManager.getScore());
        assertEquals(0, quizManager.getCorrectCount());
        assertEquals(1, quizManager.getIncorrectCount());
        assertEquals(1, quizManager.getTimedOutCount());

        QuestionReview review = quizManager.getReviewList().get(0);
        assertFalse(review.isCorrect());
        assertTrue(review.isTimedOut());
        assertEquals(-1, review.getSelectedOptionIndex());
    }

    @Test
    public void testRapidDoubleTapLock() {
        quizManager.initRound(samplePool, 10);
        Question current = quizManager.getCurrentQuestion();
        assertNotNull(current);

        int correctIndex = current.getCorrectOptionIndex();
        boolean firstTap = quizManager.submitAnswer(correctIndex);
        boolean secondTap = quizManager.submitAnswer(correctIndex);

        assertTrue("First tap should succeed", firstTap);
        assertFalse("Second rapid tap should be ignored", secondTap);
        assertEquals(1, quizManager.getScore());
        assertEquals(1, quizManager.getReviewList().size());
    }

    @Test
    public void testPercentageCalculation() {
        quizManager.initRound(samplePool, 10);

        // Answer 7 correctly and 3 incorrectly
        for (int i = 0; i < 7; i++) {
            Question q = quizManager.getCurrentQuestion();
            quizManager.submitAnswer(q.getCorrectOptionIndex());
            if (i < 6) {
                quizManager.moveToNextQuestion();
            }
        }
        quizManager.moveToNextQuestion();

        for (int i = 7; i < 10; i++) {
            Question q = quizManager.getCurrentQuestion();
            quizManager.submitAnswer((q.getCorrectOptionIndex() + 1) % 4);
            if (i < 9) {
                quizManager.moveToNextQuestion();
            }
        }

        assertEquals(7, quizManager.getScore());
        assertEquals(70, quizManager.calculatePercentage());
        assertEquals(QuizManager.PerformanceRating.GREAT, quizManager.getPerformanceRating());
    }

    @Test
    public void testPerformanceRatingTiers() {
        // Test 100% (Outstanding)
        quizManager.initRound(samplePool, 10);
        for (int i = 0; i < 10; i++) {
            Question q = quizManager.getCurrentQuestion();
            quizManager.submitAnswer(q.getCorrectOptionIndex());
            if (i < 9) quizManager.moveToNextQuestion();
        }
        assertEquals(100, quizManager.calculatePercentage());
        assertEquals(QuizManager.PerformanceRating.OUTSTANDING, quizManager.getPerformanceRating());

        // Test 40% (Keep Practicing)
        quizManager.initRound(samplePool, 10);
        for (int i = 0; i < 4; i++) {
            Question q = quizManager.getCurrentQuestion();
            quizManager.submitAnswer(q.getCorrectOptionIndex());
            if (i < 3) quizManager.moveToNextQuestion();
        }
        quizManager.moveToNextQuestion();
        for (int i = 4; i < 10; i++) {
            Question q = quizManager.getCurrentQuestion();
            quizManager.submitAnswer((q.getCorrectOptionIndex() + 1) % 4);
            if (i < 9) quizManager.moveToNextQuestion();
        }
        assertEquals(40, quizManager.calculatePercentage());
        assertEquals(QuizManager.PerformanceRating.KEEP_PRACTICING, quizManager.getPerformanceRating());
    }

    @Test
    public void testQuestionRepositoryJsonParsing() {
        QuestionRepository repo = new QuestionRepository();

        String validJson = "[\n" +
                "  {\n" +
                "    \"id\": 1,\n" +
                "    \"category\": \"Programming\",\n" +
                "    \"question\": \"What is Java?\",\n" +
                "    \"options\": [\"Language\", \"Car\", \"Fruit\", \"Planet\"],\n" +
                "    \"correctIndex\": 0,\n" +
                "    \"explanation\": \"Java is a high-level programming language.\"\n" +
                "  }\n" +
                "]";

        List<Question> parsed = repo.parseQuestionsJson(validJson);
        assertEquals(1, parsed.size());
        assertEquals("What is Java?", parsed.get(0).getQuestionText());
        assertEquals("Language", parsed.get(0).getCorrectOptionText());
    }

    @Test
    public void testCorruptedJsonDoesNotCrash() {
        QuestionRepository repo = new QuestionRepository();

        // Malformed JSON syntax
        String brokenJson = "[ { \"id\": 1, \"question\": \"Broken syntax without closing";
        List<Question> result = repo.parseQuestionsJson(brokenJson);

        assertNotNull(result);
        assertFalse("Repository should fallback on broken JSON", result.isEmpty());

        // Empty string
        List<Question> emptyResult = repo.parseQuestionsJson("");
        assertNotNull(emptyResult);
    }
}
