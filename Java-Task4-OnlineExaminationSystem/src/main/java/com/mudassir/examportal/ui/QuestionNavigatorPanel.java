package com.mudassir.examportal.ui;

import com.mudassir.examportal.service.ExamSession;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Question navigation grid allowing students to jump to any question, view real-time question states
 * (current, answered, unanswered, marked for review) and inspect progress via an explanatory legend.
 */
public class QuestionNavigatorPanel extends JPanel {

    private final Consumer<Integer> onQuestionJump;
    private final Runnable onManualSubmit;
    private final List<JButton> questionButtons = new ArrayList<>();
    private ExamSession activeSession;

    // Progress Tracker Components
    private final JLabel answeredSummaryLabel;
    private final JLabel markedSummaryLabel;
    private final JProgressBar examProgressBar;
    private final JButton navigatorSubmitButton;

    public QuestionNavigatorPanel(Consumer<Integer> onQuestionJump) {
        this(onQuestionJump, null);
    }

    public QuestionNavigatorPanel(Consumer<Integer> onQuestionJump, Runnable onManualSubmit) {
        this.onQuestionJump = onQuestionJump;
        this.onManualSubmit = onManualSubmit;

        setLayout(new BorderLayout(0, 0));
        setBackground(UITheme.COLOR_CARD_BG);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.COLOR_BORDER, 1),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));
        setPreferredSize(new Dimension(240, 0));
        setMinimumSize(new Dimension(220, 0));

        // Top Content: Header, 5x2 Grid, Legend
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);

        // Header Row with count badge
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

        JLabel titleLabel = new JLabel("Question Navigator");
        titleLabel.setFont(UITheme.FONT_TITLE_MEDIUM);
        titleLabel.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        JLabel countBadge = new JLabel("10 Qs");
        countBadge.setFont(UITheme.FONT_BADGE);
        countBadge.setForeground(UITheme.COLOR_TEXT_MUTED);
        countBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.COLOR_BORDER, 1),
                BorderFactory.createEmptyBorder(2, 6, 2, 6)
        ));

        titleRow.add(titleLabel, BorderLayout.WEST);
        titleRow.add(countBadge, BorderLayout.EAST);
        topPanel.add(titleRow);
        topPanel.add(Box.createRigidArea(new Dimension(0, 12)));

        // Center Grid for question buttons (fixed height to prevent vertical stretching)
        JPanel gridPanel = new JPanel(new GridLayout(5, 2, 8, 8));
        gridPanel.setOpaque(false);
        gridPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 222));
        gridPanel.setPreferredSize(new Dimension(212, 222));

        for (int i = 0; i < 10; i++) {
            final int index = i;
            JButton btn = new JButton(String.valueOf(i + 1));
            btn.setFont(UITheme.FONT_BODY_BOLD);
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setMargin(new Insets(2, 4, 2, 4));
            btn.setPreferredSize(new Dimension(98, 38));
            btn.addActionListener(e -> {
                if (onQuestionJump != null) {
                    onQuestionJump.accept(index);
                }
            });
            questionButtons.add(btn);
            gridPanel.add(btn);
        }
        topPanel.add(gridPanel);
        topPanel.add(Box.createRigidArea(new Dimension(0, 14)));

        // Divider
        JPanel divider1 = new JPanel();
        divider1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        divider1.setPreferredSize(new Dimension(212, 1));
        divider1.setBackground(UITheme.COLOR_BORDER);
        topPanel.add(divider1);
        topPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Legend Section
        JLabel legendTitle = new JLabel("STATUS LEGEND");
        legendTitle.setFont(UITheme.FONT_BADGE);
        legendTitle.setForeground(UITheme.COLOR_TEXT_MUTED);
        legendTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        topPanel.add(legendTitle);
        topPanel.add(Box.createRigidArea(new Dimension(0, 6)));

        JPanel legendPanel = new JPanel();
        legendPanel.setLayout(new BoxLayout(legendPanel, BoxLayout.Y_AXIS));
        legendPanel.setOpaque(false);
        legendPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        legendPanel.add(createLegendItem(new Color(219, 234, 254), UITheme.COLOR_PRIMARY, "Current Question"));
        legendPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        legendPanel.add(createLegendItem(new Color(209, 250, 229), UITheme.COLOR_STATUS_CORRECT, "Answered"));
        legendPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        legendPanel.add(createLegendItem(new Color(241, 245, 249), UITheme.COLOR_SECONDARY, "Unanswered"));
        legendPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        legendPanel.add(createLegendItem(new Color(237, 233, 254), UITheme.COLOR_REVIEW_PURPLE, "Marked for Review (*)"));

        topPanel.add(legendPanel);
        add(topPanel, BorderLayout.NORTH);

        // Bottom Content: Progress Tracker and Submit Exam action
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setOpaque(false);

        JPanel divider2 = new JPanel();
        divider2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        divider2.setPreferredSize(new Dimension(212, 1));
        divider2.setBackground(UITheme.COLOR_BORDER);
        bottomPanel.add(divider2);
        bottomPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JLabel progressTitle = new JLabel("EXAM PROGRESS");
        progressTitle.setFont(UITheme.FONT_BADGE);
        progressTitle.setForeground(UITheme.COLOR_TEXT_MUTED);
        progressTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        bottomPanel.add(progressTitle);
        bottomPanel.add(Box.createRigidArea(new Dimension(0, 6)));

        answeredSummaryLabel = new JLabel("Answered: 0 / 10");
        answeredSummaryLabel.setFont(UITheme.FONT_SMALL);
        answeredSummaryLabel.setForeground(new Color(6, 95, 70));
        answeredSummaryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        bottomPanel.add(answeredSummaryLabel);
        bottomPanel.add(Box.createRigidArea(new Dimension(0, 2)));

        markedSummaryLabel = new JLabel("Marked for Review: 0");
        markedSummaryLabel.setFont(UITheme.FONT_SMALL);
        markedSummaryLabel.setForeground(UITheme.COLOR_REVIEW_PURPLE);
        markedSummaryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        bottomPanel.add(markedSummaryLabel);
        bottomPanel.add(Box.createRigidArea(new Dimension(0, 6)));

        examProgressBar = new JProgressBar(0, 10);
        examProgressBar.setValue(0);
        examProgressBar.setStringPainted(true);
        examProgressBar.setString("0% Complete");
        examProgressBar.setFont(UITheme.FONT_BADGE);
        examProgressBar.setForeground(UITheme.COLOR_PRIMARY);
        examProgressBar.setBackground(new Color(241, 245, 249));
        examProgressBar.setPreferredSize(new Dimension(212, 18));
        examProgressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
        examProgressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        bottomPanel.add(examProgressBar);

        if (onManualSubmit != null) {
            bottomPanel.add(Box.createRigidArea(new Dimension(0, 12)));
            navigatorSubmitButton = UITheme.createPrimaryButton("Submit Exam", 'S');
            navigatorSubmitButton.setPreferredSize(new Dimension(212, 38));
            navigatorSubmitButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
            navigatorSubmitButton.setAlignmentX(Component.LEFT_ALIGNMENT);
            navigatorSubmitButton.addActionListener(e -> onManualSubmit.run());
            bottomPanel.add(navigatorSubmitButton);
        } else {
            navigatorSubmitButton = null;
        }

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createLegendItem(Color bgColor, Color textColor, String label) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        item.setOpaque(false);

        JLabel sample = new JLabel("  ");
        sample.setOpaque(true);
        sample.setBackground(bgColor);
        sample.setBorder(BorderFactory.createLineBorder(textColor, 1));
        sample.setPreferredSize(new Dimension(14, 14));

        JLabel text = new JLabel(label);
        text.setFont(UITheme.FONT_BADGE);
        text.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        item.add(sample);
        item.add(text);
        return item;
    }

    /**
     * Updates the navigator panel with the current exam session state.
     */
    public void updateState(ExamSession session) {
        this.activeSession = session;
        if (session == null) return;

        int total = session.getQuestionCount();
        int current = session.getCurrentIndex();
        int answeredCount = 0;
        int markedCount = 0;
        boolean isSessionActive = !session.isSubmitted();

        if (navigatorSubmitButton != null) {
            navigatorSubmitButton.setEnabled(isSessionActive);
        }

        for (int i = 0; i < questionButtons.size(); i++) {
            JButton btn = questionButtons.get(i);
            if (i >= total) {
                btn.setVisible(false);
                continue;
            }
            btn.setVisible(true);
            btn.setEnabled(isSessionActive);

            boolean isCurrent = (i == current);
            boolean isAnswered = session.isAnswered(i);
            boolean isMarked = session.isMarkedForReview(i);

            if (isAnswered) answeredCount++;
            if (isMarked) markedCount++;

            String text = String.valueOf(i + 1);
            if (isMarked) {
                text += " *";
            }
            btn.setText(text);

            if (isCurrent) {
                btn.setBackground(new Color(219, 234, 254)); // Light Blue
                btn.setForeground(UITheme.COLOR_PRIMARY);
                btn.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_PRIMARY, 2));
            } else if (isMarked) {
                btn.setBackground(new Color(237, 233, 254)); // Light Purple
                btn.setForeground(UITheme.COLOR_REVIEW_PURPLE);
                btn.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_REVIEW_PURPLE, 1));
            } else if (isAnswered) {
                btn.setBackground(new Color(209, 250, 229)); // Light Green
                btn.setForeground(new Color(6, 95, 70));
                btn.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_STATUS_CORRECT, 1));
            } else {
                btn.setBackground(new Color(241, 245, 249)); // Neutral Light Slate
                btn.setForeground(UITheme.COLOR_TEXT_PRIMARY);
                btn.setBorder(BorderFactory.createLineBorder(UITheme.COLOR_BORDER, 1));
            }
        }

        // Update progress tracker
        answeredSummaryLabel.setText("Answered: " + answeredCount + " / " + total);
        markedSummaryLabel.setText("Marked for Review: " + markedCount);
        examProgressBar.setMaximum(total);
        examProgressBar.setValue(answeredCount);
        int percentage = (total > 0) ? (answeredCount * 100 / total) : 0;
        examProgressBar.setString(percentage + "% Complete (" + answeredCount + "/" + total + ")");

        revalidate();
        repaint();
    }

    public List<JButton> getQuestionButtons() {
        return questionButtons;
    }

    public JButton getNavigatorSubmitButton() {
        return navigatorSubmitButton;
    }
}
