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
import com.mudassir.examportal.service.AuthService;
import com.mudassir.examportal.service.ExamService;
import com.mudassir.examportal.service.ExamSession;
import com.mudassir.examportal.service.ProfileService;
import com.mudassir.examportal.ui.ExamPanel;
import com.mudassir.examportal.ui.HeaderPanel;
import com.mudassir.examportal.ui.LoginPanel;
import com.mudassir.examportal.ui.MainFrame;
import com.mudassir.examportal.ui.ProfilePanel;
import com.mudassir.examportal.ui.ResultPanel;
import com.mudassir.examportal.ui.UITheme;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility that generates high-fidelity visual screenshot artifacts of all 10 application states.
 */
public class ScreenshotGenerator {

    private static final int FRAME_WIDTH = 1040;
    private static final int FRAME_HEIGHT = 680;

    public static void main(String[] args) throws Exception {
        // Set Look and feel
        try {
            Class<?> flatLaf = Class.forName("com.formdev.flatlaf.FlatLightLaf");
            flatLaf.getMethod("setup").invoke(null);
        } catch (Throwable t) {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }

        File projectScreenshotsDir = new File("screenshots");
        if (!projectScreenshotsDir.exists()) {
            projectScreenshotsDir.mkdirs();
        }

        // Also ensure workspace root screenshots folder exists
        File rootScreenshotsDir = new File("../../screenshots");
        if (!rootScreenshotsDir.exists()) {
            rootScreenshotsDir.mkdirs();
        }

        System.out.println("Generating comprehensive application screenshots...");

        File tempDb = File.createTempFile("screenshot_db_", ".db");
        tempDb.deleteOnExit();
        DatabaseManager dbManager = new DatabaseManager("jdbc:sqlite:" + tempDb.getAbsolutePath());
        dbManager.initialize();

        UserDao userDao = new UserDao(dbManager);
        QuestionDao questionDao = new QuestionDao(dbManager);
        AttemptDao attemptDao = new AttemptDao(dbManager);

        AuthService authService = new AuthService(userDao);
        ProfileService profileService = new ProfileService(userDao);
        ExamService examService = new ExamService(questionDao, attemptDao);

        User demoStudent = userDao.findByUsername(AppConfig.DEMO_STUDENT1_USER).orElseThrow();

        // 1. Login Screen
        MainFrame frame = new MainFrame(authService, profileService, examService);
        frame.showLogin();
        captureAndSave(frame, "01_login_screen.png", projectScreenshotsDir, rootScreenshotsDir);

        // 2. Failed Login Screen
        frame.getLoginPanel().getUsernameField().setText("student1");
        frame.getLoginPanel().getPasswordField().setText("WrongPassword999");
        frame.getLoginPanel().showError("Invalid username or password.");
        captureAndSave(frame, "02_failed_login_screen.png", projectScreenshotsDir, rootScreenshotsDir);

        // 3. Profile Screen with attempt history
        attemptDao.insertAttempt(new ExamAttempt(0, demoStudent.getId(), "2026-10-12 14:30:00", "2026-10-12 14:48:15", 8, 10, ExamStatus.SUBMITTED));
        attemptDao.insertAttempt(new ExamAttempt(0, demoStudent.getId(), "2026-10-13 09:15:00", "2026-10-13 09:45:00", 6, 10, ExamStatus.AUTO_SUBMITTED));
        frame.showProfile(demoStudent);
        captureAndSave(frame, "03_profile_screen.png", projectScreenshotsDir, rootScreenshotsDir);

        // 4. Exam Question Screen
        frame.startExam();
        ExamSession session = frame.getActiveExamSession();
        session.selectCurrentAnswer("B");
        frame.getExamPanel().loadCurrentQuestion();
        captureAndSave(frame, "04_exam_question_screen.png", projectScreenshotsDir, rootScreenshotsDir);
        // Also capture maximized display resolution to verify responsive card & grid layout
        captureAndSave(frame, "04_exam_question_screen_maximized.png", 1440, 860, projectScreenshotsDir, rootScreenshotsDir);
        frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);

        // 5. Navigator Panel with Mixed States
        // Q0 answered ('A')
        session.selectAnswer(0, "A");
        // Q1 answered ('B')
        session.selectAnswer(1, "B");
        // Q2 marked for review
        session.toggleMarkForReview(2);
        // Q3 is current
        session.jumpToQuestion(3);
        frame.getExamPanel().loadCurrentQuestion();
        captureAndSave(frame, "05_navigator_mixed_states.png", projectScreenshotsDir, rootScreenshotsDir);

        // 6. Timer Warning Screen (Amber threshold <= 5 mins and Red threshold <= 1 min)
        frame.getHeaderPanel().getTimerLabel().setDisplay("04:32", UITheme.COLOR_TIMER_AMBER);
        captureAndSave(frame, "06_timer_amber_warning.png", projectScreenshotsDir, rootScreenshotsDir);
        frame.getHeaderPanel().getTimerLabel().setDisplay("00:45", UITheme.COLOR_TIMER_RED);
        captureAndSave(frame, "06_timer_red_critical.png", projectScreenshotsDir, rootScreenshotsDir);

        // 7. Submit Confirmation Dialog
        JPanel submitDialogPanel = createMockDialogPanel(
                "Confirm Exam Submission",
                "You have 2 unanswered question(s) out of 10.\nAre you sure you want to submit your exam now?",
                JOptionPane.QUESTION_MESSAGE,
                new String[]{"Yes, Submit", "No, Keep Working"}
        );
        captureComponentImage(submitDialogPanel, 480, 200, new File(projectScreenshotsDir, "07_submit_confirmation_dialog.png"));
        captureComponentImage(submitDialogPanel, 480, 200, new File(rootScreenshotsDir, "07_submit_confirmation_dialog.png"));

        // 8. Auto-submit Dialog
        JPanel autoSubmitDialogPanel = createMockDialogPanel(
                "Time Expired",
                "Time is up! Your exam has been automatically submitted.",
                JOptionPane.WARNING_MESSAGE,
                new String[]{"OK, View Results"}
        );
        captureComponentImage(autoSubmitDialogPanel, 480, 180, new File(projectScreenshotsDir, "08_auto_submit_dialog.png"));
        captureComponentImage(autoSubmitDialogPanel, 480, 180, new File(rootScreenshotsDir, "08_auto_submit_dialog.png"));

        // 9. Result Screen with Score & Breakdown Table
        Instant tStart = Instant.parse("2026-10-14T10:00:00Z");
        java.util.concurrent.atomic.AtomicReference<Instant> clock = new java.util.concurrent.atomic.AtomicReference<>(tStart);
        ExamSession realisticSession = new ExamSession(session.getQuestions(), 1800, clock::get);
        for (int i = 0; i < 8; i++) {
            Question q = realisticSession.getQuestions().get(i);
            realisticSession.selectAnswer(i, q.getCorrectOption());
        }
        // One incorrect
        realisticSession.selectAnswer(8, "C".equals(realisticSession.getQuestions().get(8).getCorrectOption()) ? "A" : "C");
        // One unanswered (index 9)
        clock.set(tStart.plusSeconds(255)); // 04:15 elapsed
        ExamResult realisticResult = realisticSession.submit(ExamStatus.SUBMITTED);
        frame.showResult(realisticResult);
        captureAndSave(frame, "09_result_breakdown_screen.png", projectScreenshotsDir, rootScreenshotsDir);

        // 10. Quit Confirmation Dialog
        JPanel quitDialogPanel = createMockDialogPanel(
                "Quit Active Examination",
                "An examination is currently in progress.\nIf you exit now, your attempt will be marked as ABANDONED.\n\nAre you sure you want to quit?",
                JOptionPane.WARNING_MESSAGE,
                new String[]{"Yes, Abandon Exam", "No, Continue"}
        );
        captureComponentImage(quitDialogPanel, 520, 220, new File(projectScreenshotsDir, "10_quit_confirmation_dialog.png"));
        captureComponentImage(quitDialogPanel, 520, 220, new File(rootScreenshotsDir, "10_quit_confirmation_dialog.png"));

        frame.dispose();
        System.out.println("All 10 application screenshots successfully generated!");
    }

    private static void captureAndSave(JFrame frame, String filename, File dir1, File dir2) throws Exception {
        captureAndSave(frame, filename, FRAME_WIDTH, FRAME_HEIGHT, dir1, dir2);
    }

    private static void captureAndSave(JFrame frame, String filename, int width, int height, File dir1, File dir2) throws Exception {
        System.out.println("Capturing: " + filename + " (" + width + "x" + height + ")");
        frame.addNotify();
        frame.pack();
        frame.setSize(width, height);
        frame.validate();

        Container content = frame.getContentPane();
        content.setSize(width, height);
        content.validate();

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(UITheme.COLOR_APP_BG);
        g2d.fillRect(0, 0, width, height);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        content.printAll(g2d);
        g2d.dispose();

        File target1 = new File(dir1, filename);
        ImageIO.write(image, "PNG", target1);
        System.out.println("Saved: " + target1.getAbsolutePath());

        File target2 = new File(dir2, filename);
        ImageIO.write(image, "PNG", target2);
    }

    private static void captureComponentImage(Component comp, int width, int height, File file) throws Exception {
        comp.setSize(width, height);
        comp.doLayout();
        if (comp instanceof Container c) {
            c.revalidate();
        }

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        comp.paint(g2d);
        g2d.dispose();

        ImageIO.write(image, "PNG", file);
        System.out.println("Saved: " + file.getAbsolutePath());
    }

    private static JPanel createMockDialogPanel(String title, String message, int messageType, String[] buttons) {
        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                BorderFactory.createEmptyBorder(16, 20, 16, 20)
        ));

        // Header Title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UITheme.FONT_TITLE_MEDIUM);
        titleLabel.setForeground(UITheme.COLOR_TEXT_PRIMARY);
        panel.add(titleLabel, BorderLayout.NORTH);

        // Center Message
        String icon = (messageType == JOptionPane.WARNING_MESSAGE) ? "[!]" : "[?]";
        String htmlMessage = "<html><body style='font-family: Segoe UI, sans-serif; font-size: 11pt; color: #334155;'>"
                + "<table border='0' cellspacing='6'><tr>"
                + "<td style='font-size: 14pt; font-weight: bold; color: #2563EB; vertical-align: middle;'>" + icon + "</td>"
                + "<td style='vertical-align: middle; padding-left: 10px;'>" + message.replace("\n", "<br>") + "</td>"
                + "</tr></table>"
                + "</body></html>";

        JLabel msgLabel = new JLabel(htmlMessage);
        panel.add(msgLabel, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        for (int i = 0; i < buttons.length; i++) {
            JButton btn;
            if (i == 0) {
                btn = UITheme.createPrimaryButton(buttons[i], '\0');
            } else {
                btn = UITheme.createSecondaryButton(buttons[i], '\0');
            }
            btnPanel.add(btn);
        }
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }
}
