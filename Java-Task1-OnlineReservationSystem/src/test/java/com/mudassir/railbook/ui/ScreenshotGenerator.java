package com.mudassir.railbook.ui;

import com.mudassir.railbook.config.Constants;
import com.mudassir.railbook.db.DatabaseManager;
import com.mudassir.railbook.db.ReservationDao;
import com.mudassir.railbook.db.TrainDao;
import com.mudassir.railbook.db.UserDao;
import com.mudassir.railbook.model.Reservation;
import com.mudassir.railbook.model.User;
import com.mudassir.railbook.service.AuthService;
import com.mudassir.railbook.service.ReservationService;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.time.LocalDate;

/**
 * Automates rendering and exporting pixel-perfect screenshots of all major UI screens,
 * dialogs, and states for submission and documentation.
 *
 * @author Mudassir Ahmed
 */
public class ScreenshotGenerator {

    private static final String SCREENSHOTS_DIR = "screenshots";

    public static void main(String[] args) throws Exception {
        System.out.println("Generating screenshots for RailBook...");
        File dir = new File(SCREENSHOTS_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        ThemeManager.initializeLookAndFeel();

        // Setup test database
        String dbPath = "target/screenshot_railbook.db";
        File dbFile = new File(dbPath);
        if (dbFile.exists()) dbFile.delete();

        DatabaseManager dbManager = new DatabaseManager("jdbc:sqlite:" + dbPath);
        dbManager.initializeDatabase();

        UserDao userDao = new UserDao(dbManager);
        TrainDao trainDao = new TrainDao(dbManager);
        ReservationDao reservationDao = new ReservationDao(dbManager);

        AuthService authService = new AuthService(userDao);
        ReservationService reservationService = new ReservationService(reservationDao, trainDao);

        User admin = userDao.findByUsername("admin").orElseThrow();

        // Seed a sample reservation for screenshots
        String journeyDate = LocalDate.now().plusDays(5).toString();
        Reservation sampleRes = reservationService.bookTicket(
                admin.getId(),
                "Muhammad Mudassir",
                "101",
                "AC First",
                journeyDate,
                "Karachi Cantt",
                "Islamabad"
        );

        Reservation sampleRes2 = reservationService.bookTicket(
                admin.getId(),
                "Ayesha Khan",
                "103",
                "AC Business",
                LocalDate.now().plusDays(12).toString(),
                "Karachi Cantt",
                "Lahore Junction"
        );

        // 1. Login Frame
        LoginFrame loginFrame = new LoginFrame(authService, reservationService);
        captureAndSave(loginFrame, Constants.LOGIN_WINDOW_WIDTH, Constants.LOGIN_WINDOW_HEIGHT,
                SCREENSHOTS_DIR + "/01_login.png");

        // 2. Failed Login State
        setField(loginFrame, "usernameField", "admin");
        setPasswordField(loginFrame, "passwordField", "WrongPassword");
        JLabel statusLbl = (JLabel) getField(loginFrame, "statusLabel");
        statusLbl.setText("<html><center>Invalid username or password. Please try again.</center></html>");
        captureAndSave(loginFrame, Constants.LOGIN_WINDOW_WIDTH, Constants.LOGIN_WINDOW_HEIGHT,
                SCREENSHOTS_DIR + "/02_failed_login.png");

        // 3. Login Lockout State
        statusLbl.setText("Locked due to 5 failed attempts! Wait 30s");
        JButton loginBtn = (JButton) getField(loginFrame, "loginButton");
        loginBtn.setEnabled(false);
        captureAndSave(loginFrame, Constants.LOGIN_WINDOW_WIDTH, Constants.LOGIN_WINDOW_HEIGHT,
                SCREENSHOTS_DIR + "/03_login_lockout.png");

        // 4. MainFrame - Reservation Form with auto-filled train name
        MainFrame mainFrame = new MainFrame(authService, reservationService, trainDao, admin);
        JTabbedPane tabs = (JTabbedPane) getField(mainFrame, "tabbedPane");
        tabs.setSelectedIndex(0);

        ReservationPanel resPanel = (ReservationPanel) getField(mainFrame, "reservationPanel");
        JTextField nameField = (JTextField) getField(resPanel, "passengerNameField");
        JTextField trainNumField = (JTextField) getField(resPanel, "trainNumberField");
        JTextField trainNameField = (JTextField) getField(resPanel, "trainNameField");
        JComboBox<?> classCombo = (JComboBox<?>) getField(resPanel, "classTypeCombo");
        JTextField dateField = (JTextField) getField(resPanel, "journeyDateField");
        JComboBox<?> srcCombo = (JComboBox<?>) getField(resPanel, "sourceStationCombo");
        JComboBox<?> destCombo = (JComboBox<?>) getField(resPanel, "destinationStationCombo");

        nameField.setText("Muhammad Mudassir");
        trainNumField.setText("101");
        trainNameField.setText("Green Line Express");
        classCombo.setSelectedItem("AC First");
        dateField.setText(LocalDate.now().plusDays(7).toString());
        srcCombo.setSelectedItem("Karachi Cantt");
        destCombo.setSelectedItem("Islamabad");

        captureAndSave(mainFrame, Constants.MAIN_WINDOW_WIDTH, Constants.MAIN_WINDOW_HEIGHT,
                SCREENSHOTS_DIR + "/04_reservation_form.png");

        // 5. Validation Error State
        JLabel resStatus = (JLabel) getField(resPanel, "statusLabel");
        resStatus.setText("<html><font color='#dc2626'>• Source and destination stations cannot be the same.</font></html>");
        destCombo.setSelectedItem("Karachi Cantt");
        captureAndSave(mainFrame, Constants.MAIN_WINDOW_WIDTH, Constants.MAIN_WINDOW_HEIGHT,
                SCREENSHOTS_DIR + "/05_validation_error.png");

        // Reset reservation fields
        destCombo.setSelectedItem("Islamabad");
        resStatus.setText(" ");

        // 6. Confirmation Dialog
        ConfirmationDialog confirmDialog = new ConfirmationDialog(mainFrame, sampleRes, reservationService);
        captureAndSave(confirmDialog, 560, 520, SCREENSHOTS_DIR + "/06_confirmation_dialog.png");

        // 7. My Bookings Tab
        tabs.setSelectedIndex(2);
        BookingsPanel bookingsPanel = (BookingsPanel) getField(mainFrame, "bookingsPanel");
        bookingsPanel.loadBookings();
        Thread.sleep(200); // allow SwingWorker in bookingsPanel to finish
        captureAndSave(mainFrame, Constants.MAIN_WINDOW_WIDTH, Constants.MAIN_WINDOW_HEIGHT,
                SCREENSHOTS_DIR + "/07_my_bookings.png");

        // 8. Cancellation Fetch State
        tabs.setSelectedIndex(1);
        CancellationPanel cancelPanel = (CancellationPanel) getField(mainFrame, "cancellationPanel");
        JTextField pnrField = (JTextField) getField(cancelPanel, "pnrInputField");
        pnrField.setText(sampleRes.getPnr());

        // Invoke populateDetails via reflection or method
        cancelPanel.setPnrAndFetch(sampleRes.getPnr());
        Thread.sleep(200);
        captureAndSave(mainFrame, Constants.MAIN_WINDOW_WIDTH, Constants.MAIN_WINDOW_HEIGHT,
                SCREENSHOTS_DIR + "/08_cancellation_fetch.png");

        // 9. Confirm Cancellation Dialog
        JOptionPane optionPane = new JOptionPane(
                "Are you sure you want to cancel the reservation for PNR " + sampleRes.getPnr() + "?\n" +
                        "Passenger: " + sampleRes.getPassengerName() + "\n" +
                        "This action cannot be undone.",
                JOptionPane.WARNING_MESSAGE,
                JOptionPane.YES_NO_OPTION
        );
        JDialog areYouSureDialog = optionPane.createDialog(mainFrame, "Confirm Ticket Cancellation");
        captureAndSave(areYouSureDialog, 480, 200, SCREENSHOTS_DIR + "/09_confirm_cancellation_dialog.png");

        // 10. Cancellation Success Message Dialog
        JOptionPane successPane = new JOptionPane(
                "Booking for PNR " + sampleRes.getPnr() + " has been successfully cancelled.\n" +
                        "An audit record has been logged in the cancellation register.",
                JOptionPane.INFORMATION_MESSAGE,
                JOptionPane.DEFAULT_OPTION
        );
        JDialog successDialog = successPane.createDialog(mainFrame, "Cancellation Successful");
        captureAndSave(successDialog, 490, 190, SCREENSHOTS_DIR + "/10_cancellation_success.png");

        System.out.println("All 10 screenshots generated successfully in /screenshots folder!");
    }

    private static void captureAndSave(Component component, int width, int height, String targetPath) throws Exception {
        if (component instanceof JFrame frame) {
            frame.addNotify();
            frame.setSize(width, height);
            frame.doLayout();
            frame.validate();
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            frame.getRootPane().printAll(g2);
            g2.dispose();
            File outputFile = new File(targetPath);
            ImageIO.write(image, "png", outputFile);
            System.out.println("Saved: " + outputFile.getAbsolutePath() + " (" + outputFile.length() + " bytes)");
        } else if (component instanceof JDialog dialog) {
            dialog.addNotify();
            dialog.setSize(width, height);
            dialog.doLayout();
            dialog.validate();
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            dialog.getRootPane().printAll(g2);
            g2.dispose();
            File outputFile = new File(targetPath);
            ImageIO.write(image, "png", outputFile);
            System.out.println("Saved: " + outputFile.getAbsolutePath() + " (" + outputFile.length() + " bytes)");
        } else {
            component.setSize(width, height);
            component.doLayout();
            component.validate();
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            component.printAll(g2);
            g2.dispose();
            File outputFile = new File(targetPath);
            ImageIO.write(image, "png", outputFile);
            System.out.println("Saved: " + outputFile.getAbsolutePath() + " (" + outputFile.length() + " bytes)");
        }
    }

    private static Object getField(Object obj, String fieldName) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }

    private static void setField(Object obj, String fieldName, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        if (field.getType() == JTextField.class) {
            ((JTextField) field.get(obj)).setText((String) value);
        } else {
            field.set(obj, value);
        }
    }

    private static void setPasswordField(Object obj, String fieldName, String value) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        ((JPasswordField) field.get(obj)).setText(value);
    }
}
