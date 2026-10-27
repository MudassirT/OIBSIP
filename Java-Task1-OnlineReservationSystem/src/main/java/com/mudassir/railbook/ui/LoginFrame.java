package com.mudassir.railbook.ui;

import com.mudassir.railbook.config.Constants;
import com.mudassir.railbook.model.User;
import com.mudassir.railbook.service.AccountLockedException;
import com.mudassir.railbook.service.AuthService;
import com.mudassir.railbook.service.AuthenticationException;
import com.mudassir.railbook.service.ReservationService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;

/**
 * Login GUI Window for RailBook.
 * Features rate-limiting lockout protection, constant-time authentication,
 * inline error display, and keyboard responsiveness.
 *
 * @author Mudassir Ahmed
 */
public class LoginFrame extends JFrame {

    private final AuthService authService;
    private final ReservationService reservationService;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel statusLabel;
    private Timer lockoutCountdownTimer;
    private long lockoutRemainingSeconds = 0;

    public LoginFrame(AuthService authService, ReservationService reservationService) {
        this.authService = authService;
        this.reservationService = reservationService;

        initComponents();
    }

    private void initComponents() {
        setTitle(Constants.APP_NAME + " - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(Constants.LOGIN_WINDOW_WIDTH, Constants.LOGIN_WINDOW_HEIGHT);
        setMinimumSize(new Dimension(420, 480));
        setLocationRelativeTo(null);
        setResizable(false);

        // Root panel
        JPanel rootPanel = new JPanel(new BorderLayout(0, 0));
        rootPanel.setBackground(Color.WHITE);

        // Top Banner / Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ThemeManager.COLOR_PRIMARY);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(28, 24, 28, 24));

        JLabel titleLabel = new JLabel(Constants.APP_NAME, SwingConstants.CENTER);
        titleLabel.setFont(ThemeManager.FONT_TITLE);
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel(Constants.APP_SUBTITLE, SwingConstants.CENTER);
        subtitleLabel.setFont(ThemeManager.FONT_SUBTITLE);
        subtitleLabel.setForeground(new Color(224, 231, 255));

        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        rootPanel.add(headerPanel, BorderLayout.NORTH);

        // Center Form Panel
        JPanel formContainer = new JPanel(new GridBagLayout());
        formContainer.setBackground(Color.WHITE);
        formContainer.setBorder(BorderFactory.createEmptyBorder(20, 36, 16, 36));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(6, 4, 2, 4);

        // Username Label & Field
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(ThemeManager.FONT_LABEL);
        userLabel.setForeground(ThemeManager.COLOR_TEXT_DARK);
        userLabel.setDisplayedMnemonic(KeyEvent.VK_U);
        gbc.gridy = 0;
        formContainer.add(userLabel, gbc);

        usernameField = new JTextField(20);
        usernameField.setFont(ThemeManager.FONT_REGULAR);
        usernameField.setPreferredSize(new Dimension(280, 36));
        userLabel.setLabelFor(usernameField);
        gbc.gridy = 1;
        gbc.insets = new Insets(2, 4, 12, 4);
        formContainer.add(usernameField, gbc);

        // Password Label & Field
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(ThemeManager.FONT_LABEL);
        passLabel.setForeground(ThemeManager.COLOR_TEXT_DARK);
        passLabel.setDisplayedMnemonic(KeyEvent.VK_P);
        gbc.gridy = 2;
        gbc.insets = new Insets(4, 4, 2, 4);
        formContainer.add(passLabel, gbc);

        passwordField = new JPasswordField(20);
        passwordField.setFont(ThemeManager.FONT_REGULAR);
        passwordField.setPreferredSize(new Dimension(280, 36));
        passLabel.setLabelFor(passwordField);
        gbc.gridy = 3;
        gbc.insets = new Insets(2, 4, 16, 4);
        formContainer.add(passwordField, gbc);

        // Status Label (Inline error display)
        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(ThemeManager.FONT_SMALL);
        statusLabel.setForeground(ThemeManager.COLOR_DANGER);
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 4, 14, 4);
        formContainer.add(statusLabel, gbc);

        // Login Button
        loginButton = new JButton("Login to RailBook");
        loginButton.setMnemonic(KeyEvent.VK_L);
        ThemeManager.stylePrimaryButton(loginButton);
        loginButton.setPreferredSize(new Dimension(280, 40));
        loginButton.addActionListener(e -> performLogin());
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 4, 14, 4);
        formContainer.add(loginButton, gbc);

        // Demo credentials hint card
        JPanel demoCard = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
        demoCard.setBackground(ThemeManager.COLOR_CARD_BG);
        demoCard.setBorder(BorderFactory.createLineBorder(ThemeManager.COLOR_BORDER, 1));

        JLabel demoHint = new JLabel("<html><center><font color='#64748b'><b>Demo Accounts:</b><br/>" +
                "admin / Admin@123 &nbsp;|&nbsp; demo / Demo@123</font></center></html>");
        demoHint.setFont(ThemeManager.FONT_SMALL);
        demoCard.add(demoHint);

        gbc.gridy = 6;
        gbc.insets = new Insets(10, 4, 4, 4);
        formContainer.add(demoCard, gbc);

        rootPanel.add(formContainer, BorderLayout.CENTER);

        // Set Login Button as default button for Enter key
        getRootPane().setDefaultButton(loginButton);

        setContentPane(rootPanel);
    }

    private void performLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        statusLabel.setText(" ");
        loginButton.setEnabled(false);
        loginButton.setText("Verifying...");

        // Perform login in background worker to keep UI responsive
        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() throws Exception {
                return authService.login(username, password);
            }

            @Override
            protected void done() {
                loginButton.setEnabled(true);
                loginButton.setText("Login to RailBook");
                try {
                    User user = get();
                    // Login successful: open MainFrame on EDT
                    openMainFrame(user);
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    if (cause instanceof AccountLockedException lockedEx) {
                        handleAccountLockout(lockedEx.getRemainingSeconds());
                    } else if (cause instanceof AuthenticationException authEx) {
                        statusLabel.setText("<html><center>" + authEx.getMessage() + "</center></html>");
                        passwordField.setText("");
                        passwordField.requestFocusInWindow();
                    } else {
                        statusLabel.setText("An unexpected error occurred. Please try again.");
                    }
                }
            }
        };
        worker.execute();
    }

    private void handleAccountLockout(long seconds) {
        this.lockoutRemainingSeconds = seconds;
        setFormEnabled(false);
        updateLockoutLabel();

        if (lockoutCountdownTimer != null && lockoutCountdownTimer.isRunning()) {
            lockoutCountdownTimer.stop();
        }

        lockoutCountdownTimer = new Timer(1000, e -> {
            lockoutRemainingSeconds--;
            if (lockoutRemainingSeconds <= 0) {
                ((Timer) e.getSource()).stop();
                setFormEnabled(true);
                statusLabel.setText("Lockout expired. You may now attempt to login.");
                statusLabel.setForeground(ThemeManager.COLOR_SUCCESS);
            } else {
                updateLockoutLabel();
            }
        });
        lockoutCountdownTimer.start();
    }

    private void updateLockoutLabel() {
        statusLabel.setForeground(ThemeManager.COLOR_DANGER);
        statusLabel.setText("Locked due to 5 failed attempts! Wait " + lockoutRemainingSeconds + "s");
    }

    private void setFormEnabled(boolean enabled) {
        usernameField.setEnabled(enabled);
        passwordField.setEnabled(enabled);
        loginButton.setEnabled(enabled);
    }

    private void openMainFrame(User user) {
        dispose(); // close login frame
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame(authService, reservationService, user);
            mainFrame.setVisible(true);
        });
    }
}
