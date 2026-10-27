package com.mudassir.examportal.ui;

import com.mudassir.examportal.config.AppConfig;
import com.mudassir.examportal.model.User;
import com.mudassir.examportal.service.AuthService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;

/**
 * Authentication screen featuring rate limiting lockout, secure credential processing,
 * demo credentials quick-hints, and clear error messaging without disclosing specific field failures.
 */
public class LoginPanel extends JPanel {

    private final AuthService authService;
    private final Consumer<User> onLoginSuccess;

    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JButton loginButton;
    private final JLabel errorLabel;
    private final JLabel lockoutLabel;

    private Timer lockoutTimer;

    public LoginPanel(AuthService authService, Consumer<User> onLoginSuccess) {
        this.authService = authService;
        this.onLoginSuccess = onLoginSuccess;

        setLayout(new GridBagLayout());
        setBackground(UITheme.COLOR_APP_BG);

        // Center Login Card
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(420, 520));
        card.setMaximumSize(new Dimension(420, 520));

        // Header Title
        JLabel titleLabel = new JLabel("Welcome to " + AppConfig.APP_NAME);
        titleLabel.setFont(UITheme.FONT_TITLE_LARGE);
        titleLabel.setForeground(UITheme.COLOR_TEXT_PRIMARY);
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Sign in to access your examination portal");
        subtitleLabel.setFont(UITheme.FONT_BODY);
        subtitleLabel.setForeground(UITheme.COLOR_TEXT_MUTED);
        subtitleLabel.setAlignmentX(CENTER_ALIGNMENT);

        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 6)));
        card.add(subtitleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 20)));

        // Error Banner
        errorLabel = new JLabel(" ", SwingConstants.CENTER);
        errorLabel.setFont(UITheme.FONT_BODY_BOLD);
        errorLabel.setForeground(UITheme.COLOR_STATUS_INCORRECT);
        errorLabel.setAlignmentX(CENTER_ALIGNMENT);
        card.add(errorLabel);

        // Lockout Banner
        lockoutLabel = new JLabel(" ", SwingConstants.CENTER);
        lockoutLabel.setFont(UITheme.FONT_BODY_BOLD);
        lockoutLabel.setForeground(UITheme.COLOR_TIMER_AMBER);
        lockoutLabel.setAlignmentX(CENTER_ALIGNMENT);
        card.add(lockoutLabel);

        card.add(Box.createRigidArea(new Dimension(0, 15)));

        // Form fields
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setAlignmentX(CENTER_ALIGNMENT);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);

        // Username
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(UITheme.FONT_BODY_BOLD);
        userLabel.setForeground(UITheme.COLOR_TEXT_PRIMARY);
        formPanel.add(userLabel, gbc);

        gbc.gridy = 1;
        usernameField = new JTextField();
        usernameField.setFont(UITheme.FONT_BODY);
        usernameField.setPreferredSize(new Dimension(340, 38));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.COLOR_BORDER, 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        formPanel.add(usernameField, gbc);

        // Password
        gbc.gridy = 2;
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(UITheme.FONT_BODY_BOLD);
        passLabel.setForeground(UITheme.COLOR_TEXT_PRIMARY);
        formPanel.add(passLabel, gbc);

        gbc.gridy = 3;
        passwordField = new JPasswordField();
        passwordField.setFont(UITheme.FONT_BODY);
        passwordField.setPreferredSize(new Dimension(340, 38));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.COLOR_BORDER, 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        formPanel.add(passwordField, gbc);

        card.add(formPanel);
        card.add(Box.createRigidArea(new Dimension(0, 16)));

        // Login Button
        loginButton = UITheme.createPrimaryButton("Login to Portal", 'L');
        loginButton.setAlignmentX(CENTER_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(340, 42));
        loginButton.setPreferredSize(new Dimension(340, 42));
        loginButton.addActionListener(e -> performLogin());
        card.add(loginButton);

        // Enter Key Listener to submit form
        KeyAdapter enterListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && loginButton.isEnabled()) {
                    performLogin();
                }
            }
        };
        usernameField.addKeyListener(enterListener);
        passwordField.addKeyListener(enterListener);

        card.add(Box.createRigidArea(new Dimension(0, 20)));

        // Demo accounts panel
        JPanel demoPanel = new JPanel();
        demoPanel.setLayout(new BoxLayout(demoPanel, BoxLayout.Y_AXIS));
        demoPanel.setOpaque(false);
        demoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.COLOR_BORDER),
                BorderFactory.createEmptyBorder(12, 0, 0, 0)
        ));
        demoPanel.setAlignmentX(CENTER_ALIGNMENT);

        JLabel demoHeader = new JLabel("Demo Accounts (Click to Fill):");
        demoHeader.setFont(UITheme.FONT_BADGE);
        demoHeader.setForeground(UITheme.COLOR_TEXT_MUTED);
        demoHeader.setAlignmentX(CENTER_ALIGNMENT);
        demoPanel.add(demoHeader);
        demoPanel.add(Box.createRigidArea(new Dimension(0, 6)));

        JPanel demoButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        demoButtonsPanel.setOpaque(false);

        JButton demo1Btn = new JButton("student1");
        demo1Btn.setFont(UITheme.FONT_SMALL);
        demo1Btn.setToolTipText("Fill student1 / Student@123");
        demo1Btn.addActionListener(e -> {
            usernameField.setText(AppConfig.DEMO_STUDENT1_USER);
            passwordField.setText(AppConfig.DEMO_STUDENT1_PASS);
            clearError();
        });

        JButton demo2Btn = new JButton("student2");
        demo2Btn.setFont(UITheme.FONT_SMALL);
        demo2Btn.setToolTipText("Fill student2 / Student@456");
        demo2Btn.addActionListener(e -> {
            usernameField.setText(AppConfig.DEMO_STUDENT2_USER);
            passwordField.setText(AppConfig.DEMO_STUDENT2_PASS);
            clearError();
        });

        demoButtonsPanel.add(demo1Btn);
        demoButtonsPanel.add(demo2Btn);
        demoPanel.add(demoButtonsPanel);

        card.add(demoPanel);

        add(card);
    }

    private void performLogin() {
        clearError();
        String username = usernameField.getText();
        char[] password = passwordField.getPassword();

        // Disable button during operation
        loginButton.setEnabled(false);

        try {
            AuthService.AuthResult result = authService.authenticate(username, password);
            if (result.isSuccess()) {
                clearFields();
                if (onLoginSuccess != null) {
                    onLoginSuccess.accept(result.getUser());
                }
            } else {
                if (result.getLockoutRemainingSeconds() > 0) {
                    startLockoutCountdown(result.getLockoutRemainingSeconds());
                } else {
                    showError(result.getErrorMessage());
                }
            }
        } finally {
            if (!authService.isLockedOut()) {
                loginButton.setEnabled(true);
            }
        }
    }

    private void startLockoutCountdown(long seconds) {
        showError("Form locked due to repeated failed attempts.");
        loginButton.setEnabled(false);
        usernameField.setEnabled(false);
        passwordField.setEnabled(false);

        if (lockoutTimer != null) {
            lockoutTimer.stop();
        }

        lockoutTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long remaining = authService.getLockoutRemainingSeconds();
                if (remaining <= 0) {
                    lockoutTimer.stop();
                    lockoutLabel.setText(" ");
                    clearError();
                    loginButton.setEnabled(true);
                    usernameField.setEnabled(true);
                    passwordField.setEnabled(true);
                } else {
                    lockoutLabel.setText("Form locked. Try again in " + remaining + "s");
                }
            }
        });
        lockoutTimer.setRepeats(true);
        lockoutTimer.start();
        lockoutLabel.setText("Form locked. Try again in " + seconds + "s");
    }

    public void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
        clearError();
        if (lockoutTimer != null) {
            lockoutTimer.stop();
            lockoutTimer = null;
        }
        loginButton.setEnabled(true);
        usernameField.setEnabled(true);
        passwordField.setEnabled(true);
        lockoutLabel.setText(" ");
    }

    public void showError(String message) {
        errorLabel.setText(message);
    }

    public void clearError() {
        errorLabel.setText(" ");
    }

    public JButton getLoginButton() {
        return loginButton;
    }

    public JTextField getUsernameField() {
        return usernameField;
    }

    public JPasswordField getPasswordField() {
        return passwordField;
    }
}
