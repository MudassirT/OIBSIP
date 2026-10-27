package com.mudassir.examportal.ui;

import com.mudassir.examportal.config.AppConfig;
import com.mudassir.examportal.model.ExamResult;
import com.mudassir.examportal.model.ExamStatus;
import com.mudassir.examportal.model.Question;
import com.mudassir.examportal.model.User;
import com.mudassir.examportal.service.ExamService;
import com.mudassir.examportal.service.ExamSession;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Examination panel presenting one MCQ at a time, question navigation controls,
 * answer selection tracking, mark-for-review toggle, and manual/auto submission flows.
 */
public class ExamPanel extends JPanel {

    private final ExamService examService;
    private final Consumer<ExamResult> onExamSubmitted;

    private User currentUser;
    private ExamSession activeSession;

    // Header labels
    private final JLabel questionNumberLabel;
    private final JLabel categoryBadge;
    private final JCheckBox markForReviewCheckbox;
    private final Runnable onQuitExamRequested;

    // Question body
    private final JTextArea questionTextArea;

    // Options
    private final ButtonGroup optionsGroup;
    private final List<JRadioButton> optionRadios = new ArrayList<>();
    private final List<JPanel> optionCardPanels = new ArrayList<>();

    // Navigation & Submission
    private final JButton previousButton;
    private final JButton clearButton;
    private final JButton nextButton;
    private final JButton submitButton;

    // Navigator
    private final QuestionNavigatorPanel navigatorPanel;

    public ExamPanel(ExamService examService, Consumer<ExamResult> onExamSubmitted) {
        this(examService, onExamSubmitted, null);
    }

    public ExamPanel(ExamService examService, Consumer<ExamResult> onExamSubmitted, Runnable onQuitExamRequested) {
        this.examService = examService;
        this.onExamSubmitted = onExamSubmitted;
        this.onQuitExamRequested = onQuitExamRequested;

        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.COLOR_APP_BG);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // Initialize Navigator Panel first with jump callback and manual submit callback
        this.navigatorPanel = new QuestionNavigatorPanel(this::jumpToQuestion, this::handleManualSubmit);

        // Center Question Area Card
        JPanel mainQuestionCard = UITheme.createCardPanel();
        mainQuestionCard.setLayout(new BorderLayout(0, 0));

        // Top info row
        JPanel topInfoRow = new JPanel(new BorderLayout());
        topInfoRow.setOpaque(false);
        topInfoRow.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.COLOR_BORDER),
                BorderFactory.createEmptyBorder(12, 18, 12, 18)
        ));

        JPanel leftMeta = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftMeta.setOpaque(false);

        questionNumberLabel = new JLabel("Question 1 of 10");
        questionNumberLabel.setFont(UITheme.FONT_TITLE_MEDIUM);
        questionNumberLabel.setForeground(UITheme.COLOR_PRIMARY);

        categoryBadge = new JLabel("Java Fundamentals");
        categoryBadge.setFont(UITheme.FONT_BADGE);
        categoryBadge.setForeground(UITheme.COLOR_TEXT_MUTED);
        categoryBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.COLOR_BORDER, 1),
                BorderFactory.createEmptyBorder(3, 8, 3, 8)
        ));

        leftMeta.add(questionNumberLabel);
        leftMeta.add(categoryBadge);
        topInfoRow.add(leftMeta, BorderLayout.WEST);

        // Right meta: Mark for review toggle and Quit Exam button
        JPanel rightMeta = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightMeta.setOpaque(false);

        markForReviewCheckbox = new JCheckBox("Mark for Review");
        markForReviewCheckbox.setFont(UITheme.FONT_BODY_BOLD);
        markForReviewCheckbox.setForeground(UITheme.COLOR_REVIEW_PURPLE);
        markForReviewCheckbox.setOpaque(false);
        markForReviewCheckbox.setFocusPainted(false);
        markForReviewCheckbox.setCursor(new Cursor(Cursor.HAND_CURSOR));
        markForReviewCheckbox.setMnemonic('M');
        markForReviewCheckbox.addActionListener(e -> {
            if (activeSession != null) {
                activeSession.toggleCurrentMarkForReview();
                navigatorPanel.updateState(activeSession);
            }
        });
        rightMeta.add(markForReviewCheckbox);

        if (onQuitExamRequested != null) {
            JButton quitExamButton = new JButton("Quit Exam");
            quitExamButton.setFont(UITheme.FONT_BODY_BOLD);
            quitExamButton.setForeground(UITheme.COLOR_STATUS_INCORRECT);
            quitExamButton.setFocusPainted(false);
            quitExamButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            quitExamButton.setToolTipText("Abandon exam and return to dashboard");
            quitExamButton.addActionListener(e -> onQuitExamRequested.run());
            rightMeta.add(quitExamButton);
        }

        topInfoRow.add(rightMeta, BorderLayout.EAST);

        mainQuestionCard.add(topInfoRow, BorderLayout.NORTH);

        // Question and Options Content Container (grouped together to eliminate vertical stretching)
        JPanel questionAndOptionsPanel = new JPanel();
        questionAndOptionsPanel.setLayout(new BoxLayout(questionAndOptionsPanel, BoxLayout.Y_AXIS));
        questionAndOptionsPanel.setOpaque(false);
        questionAndOptionsPanel.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        // Question text area (wrapped, readable, left-aligned)
        questionTextArea = new JTextArea();
        questionTextArea.setFont(UITheme.FONT_QUESTION);
        questionTextArea.setForeground(UITheme.COLOR_TEXT_PRIMARY);
        questionTextArea.setLineWrap(true);
        questionTextArea.setWrapStyleWord(true);
        questionTextArea.setEditable(false);
        questionTextArea.setOpaque(false);
        questionTextArea.setFocusable(false);
        questionTextArea.setBorder(BorderFactory.createEmptyBorder(4, 2, 4, 2));
        questionTextArea.setAlignmentX(Component.LEFT_ALIGNMENT);

        questionAndOptionsPanel.add(questionTextArea);
        questionAndOptionsPanel.add(Box.createRigidArea(new Dimension(0, 18)));

        // Options container (full-width interactive cards)
        JPanel optionsContainer = new JPanel();
        optionsContainer.setLayout(new BoxLayout(optionsContainer, BoxLayout.Y_AXIS));
        optionsContainer.setOpaque(false);
        optionsContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

        optionsGroup = new ButtonGroup();
        String[] optionLetters = {"A", "B", "C", "D"};

        for (int i = 0; i < 4; i++) {
            final String letter = optionLetters[i];
            JPanel optionCard = new JPanel(new BorderLayout(12, 0));
            optionCard.setBackground(new Color(248, 250, 252));
            optionCard.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UITheme.COLOR_BORDER, 1),
                    BorderFactory.createEmptyBorder(10, 14, 10, 14)
            ));
            optionCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
            optionCard.setPreferredSize(new Dimension(0, 52));
            optionCard.setCursor(new Cursor(Cursor.HAND_CURSOR));
            optionCard.setAlignmentX(Component.LEFT_ALIGNMENT);

            JRadioButton radio = new JRadioButton();
            radio.setFont(UITheme.FONT_BODY);
            radio.setForeground(UITheme.COLOR_TEXT_PRIMARY);
            radio.setOpaque(false);
            radio.setFocusPainted(false);
            radio.setCursor(new Cursor(Cursor.HAND_CURSOR));

            Runnable selectAction = () -> {
                if (activeSession != null) {
                    radio.setSelected(true);
                    activeSession.selectCurrentAnswer(letter);
                    navigatorPanel.updateState(activeSession);
                    highlightOptionCards(letter);
                }
            };

            radio.addActionListener(e -> selectAction.run());

            optionCard.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    selectAction.run();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    if (activeSession == null || !letter.equalsIgnoreCase(activeSession.getCurrentAnswer())) {
                        optionCard.setBackground(new Color(241, 245, 249));
                        optionCard.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                                BorderFactory.createEmptyBorder(10, 14, 10, 14)
                        ));
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (activeSession == null || !letter.equalsIgnoreCase(activeSession.getCurrentAnswer())) {
                        optionCard.setBackground(new Color(248, 250, 252));
                        optionCard.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(UITheme.COLOR_BORDER, 1),
                                BorderFactory.createEmptyBorder(10, 14, 10, 14)
                        ));
                    }
                }
            });

            optionsGroup.add(radio);
            optionRadios.add(radio);
            optionCardPanels.add(optionCard);

            optionCard.add(radio, BorderLayout.CENTER);
            optionsContainer.add(optionCard);
            if (i < 3) {
                optionsContainer.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }

        questionAndOptionsPanel.add(optionsContainer);

        // Top-aligned wrapper to prevent vertical stretching on large monitors
        JPanel topAlignWrapper = new JPanel(new BorderLayout());
        topAlignWrapper.setOpaque(false);
        topAlignWrapper.add(questionAndOptionsPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(topAlignWrapper);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        mainQuestionCard.add(scrollPane, BorderLayout.CENTER);

        // Card Footer Bar containing navigation and submit controls
        JPanel cardFooter = new JPanel(new BorderLayout());
        cardFooter.setOpaque(false);
        cardFooter.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.COLOR_BORDER),
                BorderFactory.createEmptyBorder(12, 18, 12, 18)
        ));

        JPanel leftNav = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftNav.setOpaque(false);

        previousButton = UITheme.createSecondaryButton("Previous", 'P');
        previousButton.setPreferredSize(new Dimension(110, 38));
        previousButton.addActionListener(e -> {
            if (activeSession != null && activeSession.previousQuestion()) {
                loadCurrentQuestion();
            }
        });

        clearButton = UITheme.createSecondaryButton("Clear Answer", 'C');
        clearButton.setPreferredSize(new Dimension(125, 38));
        clearButton.addActionListener(e -> {
            if (activeSession != null) {
                activeSession.clearCurrentAnswer();
                optionsGroup.clearSelection();
                highlightOptionCards(null);
                navigatorPanel.updateState(activeSession);
            }
        });

        nextButton = UITheme.createSecondaryButton("Next", 'N');
        nextButton.setPreferredSize(new Dimension(100, 38));
        nextButton.addActionListener(e -> {
            if (activeSession != null && activeSession.nextQuestion()) {
                loadCurrentQuestion();
            }
        });

        leftNav.add(previousButton);
        leftNav.add(clearButton);
        leftNav.add(nextButton);
        cardFooter.add(leftNav, BorderLayout.WEST);

        submitButton = UITheme.createPrimaryButton("Submit Exam", 'S');
        submitButton.setPreferredSize(new Dimension(150, 38));
        submitButton.addActionListener(e -> handleManualSubmit());
        cardFooter.add(submitButton, BorderLayout.EAST);

        mainQuestionCard.add(cardFooter, BorderLayout.SOUTH);

        // Assemble primary panels: Center = Question Card, East = Navigator
        add(mainQuestionCard, BorderLayout.CENTER);
        add(navigatorPanel, BorderLayout.EAST);
    }

    /**
     * Starts an active exam attempt with the specified session.
     */
    public void startExam(User user, ExamSession session) {
        this.currentUser = user;
        this.activeSession = session;

        // Re-enable and reset all controls for the new examination attempt
        submitButton.setEnabled(true);
        clearButton.setEnabled(true);
        markForReviewCheckbox.setEnabled(true);

        for (JRadioButton radio : optionRadios) {
            radio.setEnabled(true);
        }
        for (JPanel card : optionCardPanels) {
            card.setEnabled(true);
        }

        if (navigatorPanel != null) {
            if (navigatorPanel.getNavigatorSubmitButton() != null) {
                navigatorPanel.getNavigatorSubmitButton().setEnabled(true);
            }
            for (JButton btn : navigatorPanel.getQuestionButtons()) {
                btn.setEnabled(true);
            }
        }

        loadCurrentQuestion();
    }

    /**
     * Loads the question and user selection for the current index.
     */
    public void loadCurrentQuestion() {
        if (activeSession == null) return;

        boolean isSessionActive = !activeSession.isSubmitted();

        int index = activeSession.getCurrentIndex();
        int total = activeSession.getQuestionCount();
        Question q = activeSession.getCurrentQuestion();

        questionNumberLabel.setText("Question " + (index + 1) + " of " + total);
        categoryBadge.setText(q.getCategory());
        questionTextArea.setText(q.getQuestionText());

        // Update navigation and action buttons
        previousButton.setEnabled(isSessionActive && activeSession.hasPrevious());
        nextButton.setEnabled(isSessionActive && activeSession.hasNext());
        submitButton.setEnabled(isSessionActive);
        clearButton.setEnabled(isSessionActive);
        markForReviewCheckbox.setEnabled(isSessionActive);

        for (JRadioButton radio : optionRadios) {
            radio.setEnabled(isSessionActive);
        }

        // Update Mark for review checkbox
        markForReviewCheckbox.setSelected(activeSession.isCurrentMarkedForReview());

        // Set options text
        String[] options = {q.getOptionA(), q.getOptionB(), q.getOptionC(), q.getOptionD()};
        String[] letters = {"A", "B", "C", "D"};

        optionsGroup.clearSelection();
        String currentAnswer = activeSession.getCurrentAnswer();

        for (int i = 0; i < 4; i++) {
            JRadioButton radio = optionRadios.get(i);
            radio.setText("  " + letters[i] + ".  " + options[i]);
            if (letters[i].equalsIgnoreCase(currentAnswer)) {
                radio.setSelected(true);
            }
        }

        highlightOptionCards(currentAnswer);
        navigatorPanel.updateState(activeSession);
        revalidate();
        repaint();
    }

    private void highlightOptionCards(String selectedLetter) {
        String[] letters = {"A", "B", "C", "D"};
        for (int i = 0; i < optionCardPanels.size(); i++) {
            JPanel card = optionCardPanels.get(i);
            JRadioButton radio = optionRadios.get(i);
            if (selectedLetter != null && selectedLetter.equalsIgnoreCase(letters[i])) {
                card.setBackground(new Color(239, 246, 255)); // Light Blue
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(UITheme.COLOR_PRIMARY, 2),
                        BorderFactory.createEmptyBorder(9, 13, 9, 13)
                ));
                radio.setFont(UITheme.FONT_BODY_BOLD);
                radio.setForeground(UITheme.COLOR_PRIMARY);
            } else {
                card.setBackground(new Color(248, 250, 252));
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(UITheme.COLOR_BORDER, 1),
                        BorderFactory.createEmptyBorder(10, 14, 10, 14)
                ));
                radio.setFont(UITheme.FONT_BODY);
                radio.setForeground(UITheme.COLOR_TEXT_PRIMARY);
            }
        }
    }

    private void jumpToQuestion(int index) {
        if (activeSession != null && activeSession.jumpToQuestion(index)) {
            loadCurrentQuestion();
        }
    }

    /**
     * Handles manual submission requested by the student.
     */
    private void handleManualSubmit() {
        if (activeSession == null || activeSession.isSubmitted()) return;

        int unanswered = activeSession.getUnansweredCount();
        String message;
        if (unanswered > 0) {
            message = "You have " + unanswered + " unanswered question(s) out of "
                    + activeSession.getQuestionCount() + ".\nAre you sure you want to submit your exam now?";
        } else {
            message = "You have answered all questions.\nAre you sure you want to submit your exam now?";
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                message,
                "Confirm Exam Submission",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            executeSubmission(ExamStatus.SUBMITTED);
        }
    }

    /**
     * Handles automatic submission when the countdown timer reaches zero.
     */
    public void handleAutoSubmit() {
        SwingUtilities.invokeLater(() -> {
            if (activeSession == null || activeSession.isSubmitted()) return;

            JOptionPane.showMessageDialog(
                    this,
                    "Time is up! Your exam has been automatically submitted.",
                    "Time Expired",
                    JOptionPane.WARNING_MESSAGE
            );
            executeSubmission(ExamStatus.AUTO_SUBMITTED);
        });
    }

    /**
     * Evaluates and records the exam attempt, transitioning to the Result screen.
     */
    private synchronized void executeSubmission(ExamStatus status) {
        if (activeSession == null || activeSession.isSubmitted()) return;

        submitButton.setEnabled(false);
        if (navigatorPanel != null) {
            if (navigatorPanel.getNavigatorSubmitButton() != null) {
                navigatorPanel.getNavigatorSubmitButton().setEnabled(false);
            }
            for (JButton btn : navigatorPanel.getQuestionButtons()) {
                btn.setEnabled(false);
            }
        }
        previousButton.setEnabled(false);
        nextButton.setEnabled(false);
        clearButton.setEnabled(false);
        markForReviewCheckbox.setEnabled(false);
        for (JRadioButton radio : optionRadios) {
            radio.setEnabled(false);
        }

        ExamResult result = activeSession.submit(status);
        if (currentUser != null) {
            examService.recordAttempt(currentUser, activeSession, result);
        }

        if (onExamSubmitted != null) {
            onExamSubmitted.accept(result);
        }
    }

    public ExamSession getActiveSession() {
        return activeSession;
    }

    public JButton getSubmitButton() {
        return submitButton;
    }

    public JButton getPreviousButton() {
        return previousButton;
    }

    public JButton getNextButton() {
        return nextButton;
    }

    public JButton getClearButton() {
        return clearButton;
    }

    public JCheckBox getMarkForReviewCheckbox() {
        return markForReviewCheckbox;
    }

    public List<JRadioButton> getOptionRadios() {
        return optionRadios;
    }

    public QuestionNavigatorPanel getNavigatorPanel() {
        return navigatorPanel;
    }
}
