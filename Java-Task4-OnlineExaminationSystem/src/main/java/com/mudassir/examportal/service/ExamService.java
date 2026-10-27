package com.mudassir.examportal.service;

import com.mudassir.examportal.config.AppConfig;
import com.mudassir.examportal.db.AttemptDao;
import com.mudassir.examportal.db.QuestionDao;
import com.mudassir.examportal.model.ExamAttempt;
import com.mudassir.examportal.model.ExamResult;
import com.mudassir.examportal.model.ExamStatus;
import com.mudassir.examportal.model.Question;
import com.mudassir.examportal.model.User;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

/**
 * Service managing question selection, option randomization, and exam attempt persistence.
 */
public class ExamService {

    private static final Logger LOGGER = Logger.getLogger(ExamService.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private final QuestionDao questionDao;
    private final AttemptDao attemptDao;
    private final Random random;

    public ExamService(QuestionDao questionDao, AttemptDao attemptDao) {
        this(questionDao, attemptDao, new Random());
    }

    public ExamService(QuestionDao questionDao, AttemptDao attemptDao, Random random) {
        this.questionDao = questionDao;
        this.attemptDao = attemptDao;
        this.random = (random != null) ? random : new Random();
    }

    /**
     * Initializes a new randomized ExamSession.
     * Selects up to QUESTIONS_PER_EXAM unique questions from the database, shuffles questions,
     * and randomizes options for each question while preserving correct answer integrity.
     *
     * @param durationSeconds duration of the exam attempt in seconds
     * @return initialized ExamSession
     */
    public ExamSession createExamSession(int durationSeconds) {
        List<Question> allQuestions = questionDao.findAll();
        if (allQuestions.isEmpty()) {
            throw new IllegalStateException("Question bank is empty. Cannot start exam.");
        }

        List<Question> selectedQuestions = selectAndRandomizeQuestions(allQuestions, AppConfig.QUESTIONS_PER_EXAM);
        return new ExamSession(selectedQuestions, durationSeconds);
    }

    /**
     * Selects a random subset of questions and randomizes options for each.
     */
    public List<Question> selectAndRandomizeQuestions(List<Question> pool, int count) {
        List<Question> copy = new ArrayList<>(pool);
        Collections.shuffle(copy, random);

        int sampleSize = Math.min(count, copy.size());
        List<Question> chosen = copy.subList(0, sampleSize);

        List<Question> randomized = new ArrayList<>(sampleSize);
        for (Question q : chosen) {
            randomized.add(randomizeQuestionOptions(q));
        }
        return randomized;
    }

    /**
     * Shuffles the 4 options of a question and recalculates the correct option letter.
     */
    public Question randomizeQuestionOptions(Question original) {
        String correctOptionText = original.getCorrectOptionText();

        List<String> options = new ArrayList<>(original.getOptionsList());
        Collections.shuffle(options, random);

        String optA = options.get(0);
        String optB = options.get(1);
        String optC = options.get(2);
        String optD = options.get(3);

        String newCorrectLetter = "A";
        if (optA.equals(correctOptionText)) {
            newCorrectLetter = "A";
        } else if (optB.equals(correctOptionText)) {
            newCorrectLetter = "B";
        } else if (optC.equals(correctOptionText)) {
            newCorrectLetter = "C";
        } else if (optD.equals(correctOptionText)) {
            newCorrectLetter = "D";
        }

        return new Question(
                original.getId(),
                original.getCategory(),
                original.getQuestionText(),
                optA,
                optB,
                optC,
                optD,
                newCorrectLetter
        );
    }

    /**
     * Persists a completed or abandoned exam attempt to the database.
     *
     * @param user    student who took the exam
     * @param session the exam session
     * @param result  evaluated score result
     * @return generated attempt ID
     */
    public int recordAttempt(User user, ExamSession session, ExamResult result) {
        if (user == null || session == null || result == null) {
            return -1;
        }

        String startedAt = DATE_FORMATTER.format(session.getStartTime());
        String endedAt = DATE_FORMATTER.format(session.getEndTime() != null ? session.getEndTime() : java.time.Instant.now());

        ExamAttempt attempt = new ExamAttempt(
                0,
                user.getId(),
                startedAt,
                endedAt,
                result.getScore(),
                result.getTotalQuestions(),
                result.getStatus()
        );

        int generatedId = attemptDao.insertAttempt(attempt);
        LOGGER.info("Exam attempt recorded with ID: " + generatedId + " for user: " + user.getUsername());
        return generatedId;
    }

    /**
     * Retrieves previous exam attempts made by a user.
     *
     * @param userId user ID
     * @return list of historical attempts
     */
    public List<ExamAttempt> getPreviousAttempts(int userId) {
        return attemptDao.findAttemptsByUserId(userId);
    }
}
