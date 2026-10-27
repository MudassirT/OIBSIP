package com.mudassir.examportal;

import com.mudassir.examportal.config.AppConfig;
import com.mudassir.examportal.model.ExamResult;
import com.mudassir.examportal.model.ExamStatus;
import com.mudassir.examportal.model.Question;
import com.mudassir.examportal.service.ExamSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ExamSession Business Logic, Clock Injection & Scoring Tests")
class ExamSessionTest {

    private List<Question> sampleQuestions;

    @BeforeEach
    void setUp() {
        sampleQuestions = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            sampleQuestions.add(new Question(
                    i,
                    "General CS",
                    "Question text #" + i,
                    "Option A " + i,
                    "Option B " + i,
                    "Option C " + i,
                    "Option D " + i,
                    "B" // Correct option is 'B' for all sample questions
            ));
        }
    }

    @Test
    @DisplayName("Should persist answer selection across question navigation")
    void testAnswerPersistenceDuringNavigation() {
        ExamSession session = new ExamSession(sampleQuestions, 1800);

        // Q1: select 'A'
        session.selectAnswer(0, "A");
        assertEquals("A", session.getAnswer(0));
        assertTrue(session.isAnswered(0));

        // Navigate next to Q2: select 'B'
        assertTrue(session.nextQuestion());
        assertEquals(1, session.getCurrentIndex());
        session.selectCurrentAnswer("B");
        assertEquals("B", session.getCurrentAnswer());

        // Navigate back to Q1: answer 'A' must still be selected
        assertTrue(session.previousQuestion());
        assertEquals(0, session.getCurrentIndex());
        assertEquals("A", session.getCurrentAnswer(), "Answer on Question 1 must persist when navigating back");

        // Clear answer on Q1
        session.clearCurrentAnswer();
        assertNull(session.getCurrentAnswer());
        assertFalse(session.isAnswered(0));
    }

    @Test
    @DisplayName("Should correctly toggle marked-for-review flag")
    void testMarkForReviewToggle() {
        ExamSession session = new ExamSession(sampleQuestions, 1800);

        assertFalse(session.isCurrentMarkedForReview());
        session.toggleCurrentMarkForReview();
        assertTrue(session.isCurrentMarkedForReview());
        session.toggleCurrentMarkForReview();
        assertFalse(session.isCurrentMarkedForReview());
    }

    @Test
    @DisplayName("Should accurately calculate remaining time using an injected clock")
    void testRemainingTimeWithInjectedClock() {
        Instant t0 = Instant.parse("2026-10-14T10:00:00Z");
        AtomicReference<Instant> clockRef = new AtomicReference<>(t0);

        // 30 minute exam (1800 seconds)
        ExamSession session = new ExamSession(sampleQuestions, 1800, clockRef::get);

        assertEquals(1800, session.getRemainingSeconds());
        assertFalse(session.isTimeExpired());

        // Advance clock by 10 minutes (600s) -> 20 mins remaining (1200s)
        clockRef.set(t0.plusSeconds(600));
        assertEquals(1200, session.getRemainingSeconds());
        assertFalse(session.isTimeExpired());

        // Advance clock to exact end time (1800s)
        clockRef.set(t0.plusSeconds(1800));
        assertEquals(0, session.getRemainingSeconds());
        assertTrue(session.isTimeExpired());

        // Advance clock past expiration: remaining seconds must clamp to 0
        clockRef.set(t0.plusSeconds(2000));
        assertEquals(0, session.getRemainingSeconds());
        assertTrue(session.isTimeExpired());
    }

    @Test
    @DisplayName("Should score 100% and PASS when all answers are correct")
    void testScoringAllCorrect() {
        ExamSession session = new ExamSession(sampleQuestions, 1800);

        // Correct answer for all questions is 'B'
        for (int i = 0; i < 10; i++) {
            session.selectAnswer(i, "B");
        }

        ExamResult result = session.submit(ExamStatus.SUBMITTED);
        assertEquals(10, result.getScore());
        assertEquals(10, result.getTotalQuestions());
        assertEquals(100.0, result.getPercentage());
        assertTrue(result.isPassed());
        assertEquals(10, result.getCorrectCount());
        assertEquals(0, result.getIncorrectCount());
        assertEquals(0, result.getUnansweredCount());
        assertEquals(ExamStatus.SUBMITTED, result.getStatus());
    }

    @Test
    @DisplayName("Should score 0% and FAIL when all answers are wrong")
    void testScoringAllWrong() {
        ExamSession session = new ExamSession(sampleQuestions, 1800);

        // Answer 'C' for all (correct is 'B')
        for (int i = 0; i < 10; i++) {
            session.selectAnswer(i, "C");
        }

        ExamResult result = session.submit(ExamStatus.SUBMITTED);
        assertEquals(0, result.getScore());
        assertEquals(0.0, result.getPercentage());
        assertFalse(result.isPassed());
        assertEquals(0, result.getCorrectCount());
        assertEquals(10, result.getIncorrectCount());
        assertEquals(0, result.getUnansweredCount());
    }

    @Test
    @DisplayName("Should correctly calculate partial scores, unanswered counts, and pass status")
    void testScoringPartiallyAnswered() {
        ExamSession session = new ExamSession(sampleQuestions, 1800);

        // 4 Correct ('B')
        session.selectAnswer(0, "B");
        session.selectAnswer(1, "B");
        session.selectAnswer(2, "B");
        session.selectAnswer(3, "B");

        // 2 Incorrect ('A')
        session.selectAnswer(4, "A");
        session.selectAnswer(5, "A");

        // 4 Unanswered (indices 6, 7, 8, 9)

        ExamResult result = session.submit(ExamStatus.SUBMITTED);
        assertEquals(4, result.getScore());
        assertEquals(10, result.getTotalQuestions());
        assertEquals(40.0, result.getPercentage());
        assertTrue(result.isPassed(), "40% meets PASS_PERCENTAGE (40%) threshold");
        assertEquals(4, result.getCorrectCount());
        assertEquals(2, result.getIncorrectCount());
        assertEquals(4, result.getUnansweredCount());
        assertEquals(10, result.getCorrectCount() + result.getIncorrectCount() + result.getUnansweredCount());
    }

    @Test
    @DisplayName("Submission must be strictly idempotent and subsequent submits must be ignored")
    void testSubmissionIdempotence() {
        Instant t0 = Instant.parse("2026-10-14T12:00:00Z");
        AtomicReference<Instant> clock = new AtomicReference<>(t0);

        ExamSession session = new ExamSession(sampleQuestions, 30, clock::get);
        session.selectAnswer(0, "B");

        // Advance to time zero: auto-submit triggers
        clock.set(t0.plusSeconds(30));
        ExamResult resAuto = session.submit(ExamStatus.AUTO_SUBMITTED);
        assertTrue(session.isSubmitted());
        assertEquals(ExamStatus.AUTO_SUBMITTED, resAuto.getStatus());

        // Subsequent manual submit must return identical cached result with original AUTO_SUBMITTED status
        ExamResult resManual = session.submit(ExamStatus.SUBMITTED);
        assertSame(resAuto, resManual, "Second submit must return original cached result");
        assertEquals(ExamStatus.AUTO_SUBMITTED, resManual.getStatus(), "Original status must not be overwritten");
    }

    @Test
    @DisplayName("Time taken must correctly compute duration between start and submission")
    void testTimeTakenCalculation() {
        Instant t0 = Instant.parse("2026-10-14T10:00:00Z");
        AtomicReference<Instant> clock = new AtomicReference<>(t0);

        ExamSession session = new ExamSession(sampleQuestions, 1800, clock::get);

        // Fast forward 155 seconds (2 mins 35 secs)
        clock.set(t0.plusSeconds(155));
        ExamResult result = session.submit(ExamStatus.SUBMITTED);

        assertEquals(155, result.getTimeTakenSeconds());
        assertEquals("02:35", result.getTimeTakenFormatted());
    }
}
