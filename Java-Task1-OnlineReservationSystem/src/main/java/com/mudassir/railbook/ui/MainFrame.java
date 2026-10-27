package com.mudassir.railbook.ui;

import com.mudassir.railbook.config.Constants;
import com.mudassir.railbook.db.TrainDao;
import com.mudassir.railbook.model.User;
import com.mudassir.railbook.service.AuthService;
import com.mudassir.railbook.service.ReservationService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Main application frame containing tabs for Reservation, Cancellation,
 * and My Bookings. Includes user session header, menu bar, and window exit confirmation.
 *
 * @author Mudassir Ahmed
 */
public class MainFrame extends JFrame {

    private final AuthService authService;
    private final ReservationService reservationService;
    private final TrainDao trainDao;
    private final User currentUser;

    private JTabbedPane tabbedPane;
    private ReservationPanel reservationPanel;
    private CancellationPanel cancellationPanel;
    private BookingsPanel bookingsPanel;

    public MainFrame(AuthService authService, ReservationService reservationService, User currentUser) {
        this.authService = authService;
        this.reservationService = reservationService;
        this.currentUser = currentUser;
        this.trainDao = new TrainDao(new com.mudassir.railbook.db.DatabaseManager());

        initComponents();
    }

    public MainFrame(AuthService authService, ReservationService reservationService,
                     TrainDao trainDao, User currentUser) {
        this.authService = authService;
        this.reservationService = reservationService;
        this.trainDao = trainDao;
        this.currentUser = currentUser;

        initComponents();
    }

    private void initComponents() {
        setTitle(Constants.APP_NAME + " - " + Constants.APP_SUBTITLE);
        setSize(Constants.MAIN_WINDOW_WIDTH, Constants.MAIN_WINDOW_HEIGHT);
        setMinimumSize(new Dimension(860, 560));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // Window closing confirmation
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmAndExit();
            }
        });

        // Setup Menu Bar
        setupMenuBar();

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(Color.WHITE);

        // Header Panel (Top Bar)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ThemeManager.COLOR_PRIMARY);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        // Brand Title
        JLabel brandLabel = new JLabel(Constants.APP_NAME + "  |  Train Reservation Portal");
        brandLabel.setFont(ThemeManager.FONT_HEADER);
        brandLabel.setForeground(Color.WHITE);
        headerPanel.add(brandLabel, BorderLayout.WEST);

        // User Session Controls (Right)
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        userPanel.setOpaque(false);

        JLabel userBadge = new JLabel("Logged in as: " + currentUser.getUsername());
        userBadge.setFont(ThemeManager.FONT_BOLD);
        userBadge.setForeground(new Color(224, 231, 255));

        JButton logoutBtn = new JButton("Logout");
        ThemeManager.styleSecondaryButton(logoutBtn);
        logoutBtn.setPreferredSize(new Dimension(85, 30));
        logoutBtn.addActionListener(e -> performLogout());

        userPanel.add(userBadge);
        userPanel.add(logoutBtn);
        headerPanel.add(userPanel, BorderLayout.EAST);

        rootPanel.add(headerPanel, BorderLayout.NORTH);

        // Tabbed Pane Setup
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(ThemeManager.FONT_BOLD);

        // 1. Reservation Panel
        reservationPanel = new ReservationPanel(
                reservationService,
                trainDao,
                currentUser,
                () -> {
                    // When a booking is made, refresh bookings panel
                    if (bookingsPanel != null) {
                        bookingsPanel.loadBookings();
                    }
                }
        );

        // 2. Cancellation Panel
        cancellationPanel = new CancellationPanel(
                reservationService,
                currentUser,
                () -> {
                    // When a cancellation is completed, refresh bookings panel
                    if (bookingsPanel != null) {
                        bookingsPanel.loadBookings();
                    }
                }
        );

        // 3. My Bookings Panel
        bookingsPanel = new BookingsPanel(
                reservationService,
                currentUser,
                pnr -> {
                    // When user clicks "Cancel Selected Ticket", switch to tab 1 and fetch PNR
                    tabbedPane.setSelectedIndex(1);
                    cancellationPanel.setPnrAndFetch(pnr);
                }
        );

        tabbedPane.addTab("  Book Ticket  ", reservationPanel);
        tabbedPane.addTab("  Cancel Ticket  ", cancellationPanel);
        tabbedPane.addTab("  My Bookings  ", bookingsPanel);

        // Refresh bookings whenever the user clicks on the "My Bookings" tab
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == 2 && bookingsPanel != null) {
                bookingsPanel.loadBookings();
            }
        });

        rootPanel.add(tabbedPane, BorderLayout.CENTER);

        setContentPane(rootPanel);
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File Menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setFont(ThemeManager.FONT_REGULAR);

        JMenuItem logoutItem = new JMenuItem("Logout Session");
        logoutItem.setFont(ThemeManager.FONT_REGULAR);
        logoutItem.addActionListener(e -> performLogout());

        JMenuItem exitItem = new JMenuItem("Exit Application");
        exitItem.setFont(ThemeManager.FONT_REGULAR);
        exitItem.addActionListener(e -> confirmAndExit());

        fileMenu.add(logoutItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        // View Menu
        JMenu viewMenu = new JMenu("Navigation");
        viewMenu.setFont(ThemeManager.FONT_REGULAR);

        JMenuItem bookTabItem = new JMenuItem("Go to Book Ticket");
        bookTabItem.addActionListener(e -> tabbedPane.setSelectedIndex(0));

        JMenuItem cancelTabItem = new JMenuItem("Go to Cancel Ticket");
        cancelTabItem.addActionListener(e -> tabbedPane.setSelectedIndex(1));

        JMenuItem bookingsTabItem = new JMenuItem("Go to My Bookings");
        bookingsTabItem.addActionListener(e -> tabbedPane.setSelectedIndex(2));

        viewMenu.add(bookTabItem);
        viewMenu.add(cancelTabItem);
        viewMenu.add(bookingsTabItem);

        // Help Menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setFont(ThemeManager.FONT_REGULAR);

        JMenuItem aboutItem = new JMenuItem("About RailBook");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    private void performLogout() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to log out?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (choice == JOptionPane.YES_OPTION) {
            authService.logout();
            dispose();
            SwingUtilities.invokeLater(() -> {
                LoginFrame loginFrame = new LoginFrame(authService, reservationService);
                loginFrame.setVisible(true);
            });
        }
    }

    private void confirmAndExit() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to exit " + Constants.APP_NAME + "?",
                "Confirm Exit",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
        }
    }

    private void showAboutDialog() {
        JOptionPane.showMessageDialog(
                this,
                "<html><center><h2>" + Constants.APP_NAME + " v" + Constants.APP_VERSION + "</h2>" +
                        "<p>" + Constants.APP_SUBTITLE + "</p>" +
                        "<p><b>Oasis Infobyte Internship - Java Development Task 1</b></p>" +
                        "<p>Author: <b>Mudassir Ahmed</b></p>" +
                        "<p>Built with Java 17, Swing, SQLite, FlatLaf &amp; Maven</p></center></html>",
                "About RailBook",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}
