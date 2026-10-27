package com.mudassir.examportal.ui;

import com.mudassir.examportal.config.AppConfig;
import com.mudassir.examportal.model.ExamResult;
import com.mudassir.examportal.model.ExamStatus;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

/**
 * Result evaluation screen displaying score summary, time taken, pass/fail status,
 * submission type indicator, breakdown metrics, and a detailed question-by-question review table.
 */
public class ResultPanel extends JPanel {

    private final Runnable onRetakeExam;
    private final Runnable onViewProfile;
    private final Runnable onLogout;

    private final JLabel scoreSummaryLabel;
    private final JLabel statusBadgeLabel;
    private final JLabel submissionTypeBadge;

    private final JLabel timeTakenStatLabel;
    private final JLabel correctStatLabel;
    private final JLabel incorrectStatLabel;
    private final JLabel unansweredStatLabel;

    private final DefaultTableModel breakdownTableModel;
    private final JTable breakdownTable;

    public ResultPanel(Runnable onRetakeExam, Runnable onViewProfile, Runnable onLogout) {
        this.onRetakeExam = onRetakeExam;
        this.onViewProfile = onViewProfile;
        this.onLogout = onLogout;

        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.COLOR_APP_BG);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // Top Header and Metrics Hero Card
        JPanel heroCard = UITheme.createCardPanel();
        heroCard.setLayout(new BorderLayout(12, 12));

        // Top Row: Score & Status Badges
        JPanel scoreRow = new JPanel(new BorderLayout());
        scoreRow.setOpaque(false);

        scoreSummaryLabel = new JLabel("Score: 0 / 10 (0.0%)");
        scoreSummaryLabel.setFont(UITheme.FONT_TITLE_LARGE);
        scoreSummaryLabel.setForeground(UITheme.COLOR_TEXT_PRIMARY);
        scoreRow.add(scoreSummaryLabel, BorderLayout.WEST);

        JPanel badgesPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        badgesPanel.setOpaque(false);

        submissionTypeBadge = new JLabel("Manual Submission");
        submissionTypeBadge.setFont(UITheme.FONT_BADGE);
        submissionTypeBadge.setForeground(UITheme.COLOR_PRIMARY);
        submissionTypeBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.COLOR_PRIMARY, 1),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));

        statusBadgeLabel = new JLabel("PASSED");
        statusBadgeLabel.setFont(UITheme.FONT_TITLE_MEDIUM);
        statusBadgeLabel.setForeground(UITheme.COLOR_STATUS_CORRECT);
        statusBadgeLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.COLOR_STATUS_CORRECT, 1),
                BorderFactory.createEmptyBorder(4, 12, 4, 12)
        ));

        badgesPanel.add(submissionTypeBadge);
        badgesPanel.add(statusBadgeLabel);
        scoreRow.add(badgesPanel, BorderLayout.EAST);
        heroCard.add(scoreRow, BorderLayout.NORTH);

        // Stats Grid
        JPanel statsGrid = new JPanel(new GridLayout(1, 4, 12, 0));
        statsGrid.setOpaque(false);

        timeTakenStatLabel = new JLabel("00:00", SwingConstants.CENTER);
        correctStatLabel = new JLabel("0", SwingConstants.CENTER);
        incorrectStatLabel = new JLabel("0", SwingConstants.CENTER);
        unansweredStatLabel = new JLabel("0", SwingConstants.CENTER);

        statsGrid.add(createStatBox("Time Taken", timeTakenStatLabel, UITheme.COLOR_TEXT_PRIMARY));
        statsGrid.add(createStatBox("Correct", correctStatLabel, UITheme.COLOR_STATUS_CORRECT));
        statsGrid.add(createStatBox("Incorrect", incorrectStatLabel, UITheme.COLOR_STATUS_INCORRECT));
        statsGrid.add(createStatBox("Unanswered", unansweredStatLabel, UITheme.COLOR_STATUS_UNANSWERED));

        heroCard.add(statsGrid, BorderLayout.CENTER);
        add(heroCard, BorderLayout.NORTH);

        // Center Table Card: Detailed Question Breakdown
        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(8, 8));

        JLabel tableTitle = new JLabel("Detailed Question-by-Question Breakdown");
        tableTitle.setFont(UITheme.FONT_TITLE_MEDIUM);
        tableTitle.setForeground(UITheme.COLOR_TEXT_PRIMARY);
        tableCard.add(tableTitle, BorderLayout.NORTH);

        String[] columns = {"#", "Category", "Question", "Your Answer", "Correct Answer", "Status"};
        breakdownTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        breakdownTable = new JTable(breakdownTableModel);
        breakdownTable.setFont(UITheme.FONT_BODY);
        breakdownTable.setRowHeight(32);
        breakdownTable.getTableHeader().setFont(UITheme.FONT_BODY_BOLD);
        breakdownTable.getTableHeader().setBackground(UITheme.COLOR_SECONDARY_BG);
        breakdownTable.getTableHeader().setForeground(UITheme.COLOR_TEXT_PRIMARY);

        // Column sizing
        breakdownTable.getColumnModel().getColumn(0).setPreferredWidth(35);  // #
        breakdownTable.getColumnModel().getColumn(1).setPreferredWidth(120); // Category
        breakdownTable.getColumnModel().getColumn(2).setPreferredWidth(320); // Question
        breakdownTable.getColumnModel().getColumn(3).setPreferredWidth(180); // Your Answer
        breakdownTable.getColumnModel().getColumn(4).setPreferredWidth(180); // Correct Answer
        breakdownTable.getColumnModel().getColumn(5).setPreferredWidth(110); // Status

        // Textual Status Cell Renderer with subtle background and clear text
        breakdownTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(UITheme.FONT_BODY_BOLD);
                String statusStr = (value != null) ? value.toString() : "";
                if ("Correct".equalsIgnoreCase(statusStr)) {
                    setForeground(new Color(6, 95, 70));
                    setBackground(new Color(209, 250, 229)); // Light green
                } else if ("Incorrect".equalsIgnoreCase(statusStr)) {
                    setForeground(new Color(153, 27, 27));
                    setBackground(new Color(254, 226, 226)); // Light red
                } else {
                    setForeground(new Color(71, 85, 105));
                    setBackground(new Color(241, 245, 249)); // Neutral gray
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(breakdownTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_BORDER, 1));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        add(tableCard, BorderLayout.CENTER);

        // Bottom Action Controls
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);
        bottomBar.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftActions.setOpaque(false);

        JButton logoutBtn = UITheme.createSecondaryButton("Logout", 'L');
        logoutBtn.addActionListener(e -> {
            if (onLogout != null) onLogout.run();
        });

        JButton viewProfileBtn = UITheme.createSecondaryButton("View Profile & History", 'V');
        viewProfileBtn.addActionListener(e -> {
            if (onViewProfile != null) onViewProfile.run();
        });

        leftActions.add(logoutBtn);
        leftActions.add(viewProfileBtn);
        bottomBar.add(leftActions, BorderLayout.WEST);

        JButton retakeBtn = UITheme.createPrimaryButton("Take Exam Again", 'T');
        retakeBtn.setPreferredSize(new Dimension(200, 42));
        retakeBtn.addActionListener(e -> {
            if (onRetakeExam != null) onRetakeExam.run();
        });
        bottomBar.add(retakeBtn, BorderLayout.EAST);

        add(bottomBar, BorderLayout.SOUTH);
    }

    private JPanel createStatBox(String title, JLabel valueLabel, Color valueColor) {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(new Color(248, 250, 252));
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.COLOR_BORDER, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(UITheme.FONT_BADGE);
        titleLbl.setForeground(UITheme.COLOR_TEXT_MUTED);
        titleLbl.setAlignmentX(CENTER_ALIGNMENT);

        valueLabel.setFont(UITheme.FONT_TITLE_LARGE);
        valueLabel.setForeground(valueColor);
        valueLabel.setAlignmentX(CENTER_ALIGNMENT);

        box.add(titleLbl);
        box.add(Box.createRigidArea(new Dimension(0, 4)));
        box.add(valueLabel);
        return box;
    }

    /**
     * Updates the result view with evaluated exam outcome metrics and breakdown rows.
     */
    public void displayResult(ExamResult result) {
        if (result == null) return;

        scoreSummaryLabel.setText("Score: " + result.getScoreSummary() + " (" + String.format("%.1f%%", result.getPercentage()) + ")");

        if (result.isPassed()) {
            statusBadgeLabel.setText("PASSED (" + (int) AppConfig.PASS_PERCENTAGE + "% min)");
            statusBadgeLabel.setForeground(UITheme.COLOR_STATUS_CORRECT);
            statusBadgeLabel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UITheme.COLOR_STATUS_CORRECT, 1),
                    BorderFactory.createEmptyBorder(4, 12, 4, 12)
            ));
        } else {
            statusBadgeLabel.setText("FAILED (Pass Mark: " + (int) AppConfig.PASS_PERCENTAGE + "%)");
            statusBadgeLabel.setForeground(UITheme.COLOR_STATUS_INCORRECT);
            statusBadgeLabel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UITheme.COLOR_STATUS_INCORRECT, 1),
                    BorderFactory.createEmptyBorder(4, 12, 4, 12)
            ));
        }

        if (result.isAutoSubmitted()) {
            submissionTypeBadge.setText("Auto-Submitted (Time Expired)");
            submissionTypeBadge.setForeground(UITheme.COLOR_TIMER_AMBER);
            submissionTypeBadge.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UITheme.COLOR_TIMER_AMBER, 1),
                    BorderFactory.createEmptyBorder(4, 10, 4, 10)
            ));
        } else if (result.getStatus() == ExamStatus.ABANDONED) {
            submissionTypeBadge.setText("Abandoned Attempt");
            submissionTypeBadge.setForeground(UITheme.COLOR_STATUS_INCORRECT);
            submissionTypeBadge.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UITheme.COLOR_STATUS_INCORRECT, 1),
                    BorderFactory.createEmptyBorder(4, 10, 4, 10)
            ));
        } else {
            submissionTypeBadge.setText("Manually Submitted");
            submissionTypeBadge.setForeground(UITheme.COLOR_PRIMARY);
            submissionTypeBadge.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UITheme.COLOR_PRIMARY, 1),
                    BorderFactory.createEmptyBorder(4, 10, 4, 10)
            ));
        }

        timeTakenStatLabel.setText(result.getTimeTakenFormatted());
        correctStatLabel.setText(String.valueOf(result.getCorrectCount()));
        incorrectStatLabel.setText(String.valueOf(result.getIncorrectCount()));
        unansweredStatLabel.setText(String.valueOf(result.getUnansweredCount()));

        // Populate Table
        breakdownTableModel.setRowCount(0);
        for (ExamResult.QuestionReviewItem item : result.getReviewItems()) {
            breakdownTableModel.addRow(new Object[]{
                    item.getQuestionNumber(),
                    item.getCategory(),
                    item.getQuestionText(),
                    item.getUserAnswerText(),
                    item.getCorrectAnswerText(),
                    item.getStatus()
            });
        }

        revalidate();
        repaint();
    }

    public JLabel getScoreSummaryLabel() {
        return scoreSummaryLabel;
    }

    public JLabel getStatusBadgeLabel() {
        return statusBadgeLabel;
    }

    public JLabel getSubmissionTypeBadge() {
        return submissionTypeBadge;
    }

    public JTable getBreakdownTable() {
        return breakdownTable;
    }
}
