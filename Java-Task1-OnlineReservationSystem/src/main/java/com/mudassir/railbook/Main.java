package com.mudassir.railbook;

import com.mudassir.railbook.config.Constants;
import com.mudassir.railbook.db.DatabaseManager;
import com.mudassir.railbook.db.ReservationDao;
import com.mudassir.railbook.db.TrainDao;
import com.mudassir.railbook.db.UserDao;
import com.mudassir.railbook.service.AuthService;
import com.mudassir.railbook.service.ReservationService;
import com.mudassir.railbook.ui.LoginFrame;
import com.mudassir.railbook.ui.ThemeManager;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main application entry point for RailBook Train Reservation System.
 *
 * @author Mudassir Ahmed
 */
public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        // Set standard format for java.util.logging
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tF %1$tT] [%4$-7s] %5$s %n");

        LOGGER.info("Starting " + Constants.APP_NAME + " v" + Constants.APP_VERSION + "...");

        // Initialize Look and Feel
        ThemeManager.initializeLookAndFeel();

        // Initialize Database and Services
        DatabaseManager databaseManager;
        try {
            databaseManager = new DatabaseManager();
            databaseManager.initializeDatabase();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize application database: " + e.getMessage(), e);
            JOptionPane.showMessageDialog(
                    null,
                    "Failed to initialize the database:\n" + e.getMessage() +
                            "\n\nPlease ensure you have write permissions to the application directory.",
                    Constants.APP_NAME + " - Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
            System.exit(1);
            return;
        }

        UserDao userDao = new UserDao(databaseManager);
        TrainDao trainDao = new TrainDao(databaseManager);
        ReservationDao reservationDao = new ReservationDao(databaseManager);

        AuthService authService = new AuthService(userDao);
        ReservationService reservationService = new ReservationService(reservationDao, trainDao);

        // Launch UI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                LoginFrame loginFrame = new LoginFrame(authService, reservationService);
                loginFrame.setVisible(true);
                LOGGER.info("RailBook GUI successfully presented on the Event Dispatch Thread.");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Unexpected error launching GUI: " + e.getMessage(), e);
                JOptionPane.showMessageDialog(
                        null,
                        "An unexpected error occurred while launching the application:\n" + e.getMessage(),
                        Constants.APP_NAME + " - Startup Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
}
