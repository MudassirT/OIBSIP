package com.mudassir.examportal.ui;

import com.mudassir.examportal.config.AppConfig;
import com.mudassir.examportal.model.ExamAttempt;
import com.mudassir.examportal.model.User;
import com.mudassir.examportal.service.ExamService;
import com.mudassir.examportal.service.ProfileService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.function.Consumer;

/**
 * Profile management screen showing student settings, previous exam attempts history,
 * exam rules/instructions, and the launch trigger for starting a fresh exam attempt.
 */
public class ProfilePanel extends JPanel {

    private final ProfileService profileService;
    private final ExamService examService;
    private final Consumer<String> onDisplayNameUpdated;
    private final Runnable onStartExamRequested;
    private final Runnable onLogoutRequested;

    private User currentUser;

    private final JTextField displayNameField;
    private final JPasswordField currentPasswordField;
    private final JPasswordField newPasswordField;
    private final JPasswordField confirmPasswordField;
    private final JLabel statusMessageLabel;

    private final DefaultTableModel attemptsTableModel;
    private final JTable attemptsTable;

    public ProfilePanel(ProfileService profileService,
                        ExamService examService,
                        Consumer<String> onDisplayNameUpdated,
                        Runnable onStartExamRequested,
                        Runnable onLogoutRequested) {
        this.profileService = profileService;
        this.examService = examService;
        this.onDisplayNameUpdated = onDisplayNameUpdated;
        this.onStartExamRequested = onStartExamRequested;
        this.onLogoutRequested = onLogoutRequested;

        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.COLOR_APP_BG);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // Top Instructions Banner
        add(createInstructionsCard(), BorderLayout.NORTH);

        // Center Split: Left = Profile Update, Right = Previous Attempts Table
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 6, 0, 6);
        gbc.weighty = 1.0;

        // Left Card: Profile Form
        gbc.gridx = 0;
        gbc.weightx = 0.48;
        JPanel profileCard = createProfileFormCard();
        centerPanel.add(profileCard, gbc);

        // Right Card: Attempts History
        gbc.gridx = 1;
        gbc.weightx = 0.52;
        JPanel historyCard = createHistoryCard();
        centerPanel.add(historyCard, gbc);

        add(centerPanel, BorderLayout.CENTER);

        // Bottom Action Bar: Start Exam & Logout
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);
        bottomBar.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        JButton logoutButton = UITheme.createSecondaryButton("Logout", 'L');
        logoutButton.addActionListener(e -> {
            if (hasUnsavedChanges()) {
                int res = JOptionPane.showConfirmDialog(this,
                        "You have unsaved changes on your profile. Do you really want to logout?",
                        "Unsaved Changes", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (res != JOptionPane.YES_OPTION) return;
            }
            if (onLogoutRequested != null) onLogoutRequested.run();
        });
        bottomBar.add(logoutButton, BorderLayout.WEST);

        JButton startExamButton = UITheme.createPrimaryButton("Start Examination", 'S');
        startExamButton.setPreferredSize(new Dimension(220, 44));
        startExamButton.addActionListener(e -> handleStartExam());
        bottomBar.add(startExamButton, BorderLayout.EAST);

        add(bottomBar, BorderLayout.SOUTH);

        // Initial component state
        displayNameField = new JTextField();
        currentPasswordField = new JPasswordField();
        newPasswordField = new JPasswordField();
        confirmPasswordField = new JPasswordField();
        statusMessageLabel = new JLabel(" ");
        attemptsTableModel = null; // initialized in createHistoryCard
        attemptsTable = null;      // initialized in createHistoryCard
    }

    private JPanel createInstructionsCard() {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(12, 6));

        JLabel title = new JLabel("Examination Instructions & Rules");
        title.setFont(UITheme.FONT_TITLE_MEDIUM);
        title.setForeground(UITheme.COLOR_TEXT_PRIMARY);
        card.add(title, BorderLayout.NORTH);

        int durationMins = AppConfig.getExamDurationSeconds() / 60;
        int durationSecs = AppConfig.getExamDurationSeconds() % 60;
        String durationStr = (durationMins > 0 ? durationMins + " minutes" : "")
                + (durationSecs > 0 ? " " + durationSecs + " seconds" : "");

        String instructionsHtml = "<html><body style='font-family: Segoe UI, sans-serif; font-size: 11pt; color: #475569;'>"
                + "• <b>Total Questions:</b> " + AppConfig.QUESTIONS_PER_EXAM + " Multiple Choice Questions (randomized from question bank)<br>"
                + "• <b>Allotted Duration:</b> " + durationStr + " (timer counts down continuously in header)<br>"
                + "• <b>Passing Criteria:</b> " + (int) AppConfig.PASS_PERCENTAGE + "% passing threshold<br>"
                + "• <b>Auto-Submission:</b> When the countdown timer reaches <b>00:00</b>, the exam will automatically submit immediately.<br>"
                + "• <b>Navigation:</b> You may jump to any question using the navigator panel or review marked questions before submitting."
                + "</body></html>";

        JLabel contentLabel = new JLabel(instructionsHtml);
        card.add(contentLabel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createProfileFormCard() {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("Student Profile Settings");
        title.setFont(UITheme.FONT_TITLE_MEDIUM);
        title.setForeground(UITheme.COLOR_TEXT_PRIMARY);
        card.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(4, 4, 4, 4);
        g.weightx = 1.0;

        // Display Name
        g.gridy = 0;
        g.insets = new Insets(4, 4, 6, 4);
        JLabel nameLbl = new JLabel("Display Name (2-40 chars, letters/spaces):");
        nameLbl.setFont(UITheme.FONT_BODY_BOLD);
        form.add(nameLbl, g);

        g.gridy = 1;
        g.insets = new Insets(0, 4, 12, 4);
        displayNameFieldRef = new JTextField();
        displayNameFieldRef.setFont(UITheme.FONT_BODY);
        displayNameFieldRef.setPreferredSize(new Dimension(280, 32));
        form.add(displayNameFieldRef, g);

        // Change Password Section
        g.gridy = 2;
        g.insets = new Insets(8, 4, 8, 4);
        JLabel pwdHeader = new JLabel("Change Password (Optional):");
        pwdHeader.setFont(UITheme.FONT_BODY_BOLD);
        pwdHeader.setForeground(UITheme.COLOR_PRIMARY);
        form.add(pwdHeader, g);

        g.gridy = 3;
        g.insets = new Insets(4, 4, 4, 4);
        form.add(new JLabel("Current Password:"), g);

        g.gridy = 4;
        g.insets = new Insets(0, 4, 10, 4);
        currentPasswordFieldRef = new JPasswordField();
        currentPasswordFieldRef.setFont(UITheme.FONT_BODY);
        currentPasswordFieldRef.setPreferredSize(new Dimension(280, 32));
        form.add(currentPasswordFieldRef, g);

        g.gridy = 5;
        g.insets = new Insets(4, 4, 4, 4);
        form.add(new JLabel("New Password (min 8 chars, 1 letter & 1 number):"), g);

        g.gridy = 6;
        g.insets = new Insets(0, 4, 10, 4);
        newPasswordFieldRef = new JPasswordField();
        newPasswordFieldRef.setFont(UITheme.FONT_BODY);
        newPasswordFieldRef.setPreferredSize(new Dimension(280, 32));
        form.add(newPasswordFieldRef, g);

        g.gridy = 7;
        g.insets = new Insets(4, 4, 4, 4);
        form.add(new JLabel("Confirm New Password:"), g);

        g.gridy = 8;
        g.insets = new Insets(0, 4, 10, 4);
        confirmPasswordFieldRef = new JPasswordField();
        confirmPasswordFieldRef.setFont(UITheme.FONT_BODY);
        confirmPasswordFieldRef.setPreferredSize(new Dimension(280, 32));
        form.add(confirmPasswordFieldRef, g);

        // Status message
        g.gridy = 9;
        g.insets = new Insets(4, 4, 4, 4);
        statusMessageLabelRef = new JLabel(" ", SwingConstants.LEFT);
        statusMessageLabelRef.setFont(UITheme.FONT_SMALL);
        form.add(statusMessageLabelRef, g);

        // Vertical spacer to anchor fields to the top on tall or maximized displays
        g.gridy = 10;
        g.weighty = 1.0;
        JPanel verticalSpacer = new JPanel();
        verticalSpacer.setOpaque(false);
        form.add(verticalSpacer, g);

        card.add(form, BorderLayout.CENTER);

        // Save Button
        JButton saveButton = UITheme.createSecondaryButton("Save Profile Changes", 'C');
        saveButton.addActionListener(e -> handleSaveChanges());
        JPanel savePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        savePanel.setOpaque(false);
        savePanel.add(saveButton);
        card.add(savePanel, BorderLayout.SOUTH);

        return card;
    }

    private JTextField displayNameFieldRef;
    private JPasswordField currentPasswordFieldRef;
    private JPasswordField newPasswordFieldRef;
    private JPasswordField confirmPasswordFieldRef;
    private JLabel statusMessageLabelRef;

    private DefaultTableModel attemptsTableModelRef;
    private JTable attemptsTableRef;

    private JPanel createHistoryCard() {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(8, 8));

        JLabel title = new JLabel("Previous Exam Attempts");
        title.setFont(UITheme.FONT_TITLE_MEDIUM);
        title.setForeground(UITheme.COLOR_TEXT_PRIMARY);
        card.add(title, BorderLayout.NORTH);

        String[] columns = {"Date & Time", "Score", "Percentage", "Status"};
        attemptsTableModelRef = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        attemptsTableRef = new JTable(attemptsTableModelRef);
        attemptsTableRef.setFont(UITheme.FONT_BODY);
        attemptsTableRef.setRowHeight(26);
        attemptsTableRef.getTableHeader().setFont(UITheme.FONT_BODY_BOLD);
        attemptsTableRef.getTableHeader().setBackground(UITheme.COLOR_SECONDARY_BG);
        attemptsTableRef.getTableHeader().setForeground(UITheme.COLOR_TEXT_PRIMARY);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        attemptsTableRef.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        attemptsTableRef.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        attemptsTableRef.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(attemptsTableRef);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_BORDER, 1));
        card.add(scrollPane, BorderLayout.CENTER);

        return card;
    }

    /**
     * Initializes the profile screen with the authenticated user data and previous attempts.
     */
    public void setUser(User user) {
        this.currentUser = user;
        if (user != null) {
            displayNameFieldRef.setText(user.getDisplayName());
            currentPasswordFieldRef.setText("");
            newPasswordFieldRef.setText("");
            confirmPasswordFieldRef.setText("");
            statusMessageLabelRef.setText(" ");
            refreshAttemptsHistory();
        }
    }

    public void refreshAttemptsHistory() {
        if (currentUser == null || attemptsTableModelRef == null) return;
        attemptsTableModelRef.setRowCount(0);

        List<ExamAttempt> attempts = examService.getPreviousAttempts(currentUser.getId());
        if (attempts.isEmpty()) {
            attemptsTableModelRef.addRow(new Object[]{"No previous attempts yet", "-", "-", "-"});
        } else {
            for (ExamAttempt a : attempts) {
                attemptsTableModelRef.addRow(new Object[]{
                        a.getStartedAt(),
                        a.getScore() + " / " + a.getTotalQuestions(),
                        String.format("%.1f%%", a.getPercentage()),
                        a.getStatus().getDisplayName()
                });
            }
        }
    }

    private void handleSaveChanges() {
        if (currentUser == null) return;
        statusMessageLabelRef.setText(" ");

        boolean nameChanged = !displayNameFieldRef.getText().trim().equals(currentUser.getDisplayName());
        boolean passwordInputPresent = currentPasswordFieldRef.getPassword().length > 0
                || newPasswordFieldRef.getPassword().length > 0
                || confirmPasswordFieldRef.getPassword().length > 0;

        if (!nameChanged && !passwordInputPresent) {
            statusMessageLabelRef.setForeground(UITheme.COLOR_TEXT_MUTED);
            statusMessageLabelRef.setText("No changes detected.");
            return;
        }

        // Process Display Name Change
        if (nameChanged) {
            String newName = displayNameFieldRef.getText().trim();
            ProfileService.ProfileResult nameRes = profileService.updateDisplayName(currentUser, newName);
            if (!nameRes.isSuccess()) {
                statusMessageLabelRef.setForeground(UITheme.COLOR_STATUS_INCORRECT);
                statusMessageLabelRef.setText(nameRes.getMessage());
                return;
            }
            if (onDisplayNameUpdated != null) {
                onDisplayNameUpdated.accept(currentUser.getDisplayName());
            }
        }

        // Process Password Change
        if (passwordInputPresent) {
            char[] curPass = currentPasswordFieldRef.getPassword();
            char[] newPass = newPasswordFieldRef.getPassword();
            char[] confPass = confirmPasswordFieldRef.getPassword();

            ProfileService.ProfileResult passRes = profileService.updatePassword(currentUser, curPass, newPass, confPass);
            if (!passRes.isSuccess()) {
                statusMessageLabelRef.setForeground(UITheme.COLOR_STATUS_INCORRECT);
                statusMessageLabelRef.setText(passRes.getMessage());
                return;
            }
            currentPasswordFieldRef.setText("");
            newPasswordFieldRef.setText("");
            confirmPasswordFieldRef.setText("");
        }

        statusMessageLabelRef.setForeground(UITheme.COLOR_STATUS_CORRECT);
        statusMessageLabelRef.setText("Profile changes saved successfully!");
    }

    public boolean hasUnsavedChanges() {
        if (currentUser == null) return false;
        boolean nameChanged = !displayNameFieldRef.getText().trim().equals(currentUser.getDisplayName());
        boolean passwordInputPresent = currentPasswordFieldRef.getPassword().length > 0
                || newPasswordFieldRef.getPassword().length > 0
                || confirmPasswordFieldRef.getPassword().length > 0;
        return nameChanged || passwordInputPresent;
    }

    private void handleStartExam() {
        if (hasUnsavedChanges()) {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "You have unsaved profile changes. Do you want to proceed to the exam without saving?",
                    "Unsaved Profile Edits",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
        }

        if (onStartExamRequested != null) {
            onStartExamRequested.run();
        }
    }

    public JTextField getDisplayNameField() {
        return displayNameFieldRef;
    }

    public JPasswordField getCurrentPasswordField() {
        return currentPasswordFieldRef;
    }

    public JPasswordField getNewPasswordField() {
        return newPasswordFieldRef;
    }

    public JPasswordField getConfirmPasswordField() {
        return confirmPasswordFieldRef;
    }

    public JLabel getStatusMessageLabel() {
        return statusMessageLabelRef;
    }
}
