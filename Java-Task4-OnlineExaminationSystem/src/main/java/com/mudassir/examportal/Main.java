package com.mudassir.examportal;

import com.mudassir.examportal.config.AppConfig;
import com.mudassir.examportal.db.AttemptDao;
import com.mudassir.examportal.db.DatabaseManager;
import com.mudassir.examportal.db.QuestionDao;
import com.mudassir.examportal.db.UserDao;
import com.mudassir.examportal.service.AuthService;
import com.mudassir.examportal.service.ExamService;
import com.mudassir.examportal.service.ProfileService;
import com.mudassir.examportal.ui.MainFrame;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Application entry point for the ExamPortal Online Examination System.
 * Initializes Look-and-Feel, sets up database schema and seed data, and launches the UI on the Swing EDT.
 */
public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        // Configure Look and Feel: FlatLaf with fallback to System Look and Feel
        setupLookAndFeel();

        // Launch application on Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                LOGGER.info("Starting " + AppConfig.APP_TITLE + "...");

                // Initialize Database and Seed Data
                DatabaseManager dbManager = new DatabaseManager();
                dbManager.initialize();

                // Initialize DAOs
                UserDao userDao = new UserDao(dbManager);
                QuestionDao questionDao = new QuestionDao(dbManager);
                AttemptDao attemptDao = new AttemptDao(dbManager);

                // Initialize Business Services
                AuthService authService = new AuthService(userDao);
                ProfileService profileService = new ProfileService(userDao);
                ExamService examService = new ExamService(questionDao, attemptDao);

                // Initialize and Display Main GUI Frame
                MainFrame frame = new MainFrame(authService, profileService, examService);
                frame.setVisible(true);

                LOGGER.info(AppConfig.APP_NAME + " launched successfully.");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Fatal error launching application", e);
                JOptionPane.showMessageDialog(
                        null,
                        "Failed to initialize ExamPortal database or resources:\n" + e.getMessage()
                                + "\n\nPlease ensure you have write permissions to the application folder.",
                        "Startup Error",
                        JOptionPane.ERROR_MESSAGE
                );
                System.exit(1);
            }
        });
    }

    /**
     * Attempts to configure FlatLaf Light, gracefully falling back to the OS System Look and Feel.
     */
    private static void setupLookAndFeel() {
        try {
            // Attempt FlatLaf initialization via reflection or direct class
            Class<?> flatLafClass = Class.forName("com.formdev.flatlaf.FlatLightLaf");
            java.lang.reflect.Method setupMethod = flatLafClass.getMethod("setup");
            setupMethod.invoke(null);
            LOGGER.info("FlatLaf Light look and feel initialized.");
        } catch (Throwable t) {
            LOGGER.warning("FlatLaf not found or failed to load. Falling back to system look and feel: " + t.getMessage());
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Keep default cross-platform Metal look and feel if system look and feel fails
            }
        }
    }
}
