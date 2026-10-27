package com.mudassir.examportal.ui;

import com.mudassir.examportal.config.AppConfig;
import com.mudassir.examportal.model.ExamResult;
import com.mudassir.examportal.model.ExamStatus;
import com.mudassir.examportal.model.User;
import com.mudassir.examportal.service.AuthService;
import com.mudassir.examportal.service.ExamService;
import com.mudassir.examportal.service.ExamSession;
import com.mudassir.examportal.service.ProfileService;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Logger;

/**
 * Primary application window managing the CardLayout view transitions,
 * window-closing confirmations, and session state lifecycles.
 */
public class MainFrame extends JFrame {

    private static final Logger LOGGER = Logger.getLogger(MainFrame.class.getName());

    private final AuthService authService;
    private final ProfileService profileService;
    private final ExamService examService;

    private User currentUser;
    private ExamSession activeExamSession;
    private String currentCard = AppConfig.CARD_LOGIN;

    private final HeaderPanel headerPanel;
    private final JPanel cardsContainer;
    private final CardLayout cardLayout;

    private final LoginPanel loginPanel;
    private final ProfilePanel profilePanel;
    private final ExamPanel examPanel;
    private final ResultPanel resultPanel;

    public MainFrame(AuthService authService, ProfileService profileService, ExamService examService) {
        super(AppConfig.APP_TITLE);
        this.authService = authService;
        this.profileService = profileService;
        this.examService = examService;

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1040, 680);
        setMinimumSize(new Dimension(960, 620));
        setLocationRelativeTo(null); // Centered on screen

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleWindowClose();
            }
        });

        // Main layout container
        setLayout(new BorderLayout());
        setBackground(UITheme.COLOR_APP_BG);

        // Header
        headerPanel = new HeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Cards Container
        cardLayout = new CardLayout();
        cardsContainer = new JPanel(cardLayout);
        cardsContainer.setBackground(UITheme.COLOR_APP_BG);

        // Child Panels
        loginPanel = new LoginPanel(authService, this::onLoginSuccess);
        profilePanel = new ProfilePanel(
                profileService,
                examService,
                this::onDisplayNameUpdated,
                this::onStartExamRequested,
                this::logout
        );
        examPanel = new ExamPanel(examService, this::onExamSubmitted, this::handleQuitExam);
        resultPanel = new ResultPanel(
                this::onStartExamRequested,
                () -> showProfile(currentUser),
                this::logout
        );

        cardsContainer.add(loginPanel, AppConfig.CARD_LOGIN);
        cardsContainer.add(profilePanel, AppConfig.CARD_PROFILE);
        cardsContainer.add(examPanel, AppConfig.CARD_EXAM);
        cardsContainer.add(resultPanel, AppConfig.CARD_RESULT);

        add(cardsContainer, BorderLayout.CENTER);

        // Show login initially
        showLogin();
    }

    public void showLogin() {
        this.currentUser = null;
        this.activeExamSession = null;
        this.currentCard = AppConfig.CARD_LOGIN;

        headerPanel.setUser(null);
        headerPanel.showTimer(false);

        loginPanel.clearFields();
        cardLayout.show(cardsContainer, AppConfig.CARD_LOGIN);
        getRootPane().setDefaultButton(loginPanel.getLoginButton());
    }

    public void showProfile(User user) {
        this.currentUser = user;
        this.currentCard = AppConfig.CARD_PROFILE;

        headerPanel.setUser(user);
        headerPanel.showTimer(false);

        profilePanel.setUser(user);
        cardLayout.show(cardsContainer, AppConfig.CARD_PROFILE);
        getRootPane().setDefaultButton(null);
    }

    public void startExam() {
        if (currentUser == null) {
            showLogin();
            return;
        }

        int durationSeconds = AppConfig.getExamDurationSeconds();
        activeExamSession = examService.createExamSession(durationSeconds);
        this.currentCard = AppConfig.CARD_EXAM;

        headerPanel.setUser(currentUser);
        headerPanel.showTimer(true);

        // Start countdown timer linked to examPanel auto-submit
        headerPanel.getTimerLabel().startTimer(durationSeconds, examPanel::handleAutoSubmit);

        examPanel.startExam(currentUser, activeExamSession);
        cardLayout.show(cardsContainer, AppConfig.CARD_EXAM);
        getRootPane().setDefaultButton(null);
    }

    public void showResult(ExamResult result) {
        this.currentCard = AppConfig.CARD_RESULT;

        headerPanel.showTimer(false);
        resultPanel.displayResult(result);
        cardLayout.show(cardsContainer, AppConfig.CARD_RESULT);
    }

    private void onLoginSuccess(User user) {
        showProfile(user);
    }

    private void onDisplayNameUpdated(String newDisplayName) {
        headerPanel.updateDisplayName(newDisplayName);
    }

    private void onStartExamRequested() {
        startExam();
    }

    private void onExamSubmitted(ExamResult result) {
        headerPanel.showTimer(false);
        showResult(result);
    }

    private long lastCloseClickTimestamp = 0;
    private int closeClickCount = 0;

    public void logout() {
        if (headerPanel != null) {
            headerPanel.showTimer(false);
        }
        showLogin();
    }

    /**
     * Handles window close requests with rapid multi-click emergency exit support
     * and safe abandonment options during active exams.
     */
    private void handleWindowClose() {
        long now = System.currentTimeMillis();
        if (now - lastCloseClickTimestamp < 2500) {
            closeClickCount++;
        } else {
            closeClickCount = 1;
        }
        lastCloseClickTimestamp = now;

        // If the user clicks 'X' multiple times (>= 2 clicks within 2.5s), force exit immediately!
        if (closeClickCount >= 2) {
            LOGGER.warning("Multiple window close clicks detected (" + closeClickCount + "). Forcing immediate exit.");
            forceExit();
            return;
        }

        if (AppConfig.CARD_EXAM.equals(currentCard) && activeExamSession != null && !activeExamSession.isSubmitted()) {
            Object[] options = {
                    "Leave Exam & Return to Dashboard",
                    "Exit Application",
                    "Continue Exam"
            };
            int choice = JOptionPane.showOptionDialog(
                    this,
                    "An examination is currently in progress.\nIf you leave now, your attempt will be recorded as ABANDONED.\n\nWhat would you like to do?",
                    "Close Window / Quit Exam",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null,
                    options,
                    options[2]
            );

            if (choice == 0) {
                // Leave Exam & Return to Dashboard
                quitExamAndReturnToProfile(false);
            } else if (choice == 1) {
                // Exit Application completely
                forceExit();
            }
            // choice == 2 or CLOSED_OPTION (-1): Continue Exam
        } else {
            // Outside of active exam, close immediately
            forceExit();
        }
    }

    /**
     * Handles student request to leave the active exam and return to the profile dashboard.
     */
    public void handleQuitExam() {
        quitExamAndReturnToProfile(true);
    }

    /**
     * Quits the current exam, records it as ABANDONED, and returns to profile dashboard.
     */
    public void quitExamAndReturnToProfile(boolean confirmFirst) {
        if (activeExamSession != null && !activeExamSession.isSubmitted()) {
            if (confirmFirst) {
                int choice = JOptionPane.showConfirmDialog(
                        this,
                        "Are you sure you want to leave this exam?\nYour current attempt will be recorded as ABANDONED.",
                        "Quit Exam",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );
                if (choice != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            LOGGER.info("Exam quit by user. Submitting as ABANDONED and returning to profile.");
            if (headerPanel != null) {
                headerPanel.showTimer(false);
            }
            ExamResult result = activeExamSession.submit(ExamStatus.ABANDONED);
            if (currentUser != null) {
                examService.recordAttempt(currentUser, activeExamSession, result);
            }
        }
        showProfile(currentUser);
    }

    /**
     * Safely tears down the application, recording abandonment if in exam, and exits the JVM.
     */
    public void forceExit() {
        try {
            if (activeExamSession != null && !activeExamSession.isSubmitted()) {
                LOGGER.info("Force exiting while exam active. Recording attempt as ABANDONED.");
                ExamResult result = activeExamSession.submit(ExamStatus.ABANDONED);
                if (currentUser != null) {
                    examService.recordAttempt(currentUser, activeExamSession, result);
                }
            }
            if (headerPanel != null) {
                headerPanel.showTimer(false);
            }
        } catch (Exception ex) {
            LOGGER.warning("Error during forceExit cleanup: " + ex.getMessage());
        } finally {
            dispose();
            System.exit(0);
        }
    }

    public String getCurrentCard() {
        return currentCard;
    }

    public HeaderPanel getHeaderPanel() {
        return headerPanel;
    }

    public LoginPanel getLoginPanel() {
        return loginPanel;
    }

    public ProfilePanel getProfilePanel() {
        return profilePanel;
    }

    public ExamPanel getExamPanel() {
        return examPanel;
    }

    public ResultPanel getResultPanel() {
        return resultPanel;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public ExamSession getActiveExamSession() {
        return activeExamSession;
    }
}
