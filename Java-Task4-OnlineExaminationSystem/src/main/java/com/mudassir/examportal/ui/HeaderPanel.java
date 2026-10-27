package com.mudassir.examportal.ui;

import com.mudassir.examportal.config.AppConfig;
import com.mudassir.examportal.model.User;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

/**
 * Top header banner visible across screens, displaying the application brand,
 * the active student's display name, and the active countdown timer during an exam.
 */
public class HeaderPanel extends JPanel {

    private final JLabel appTitleLabel;
    private final JLabel userGreetingLabel;
    private final JPanel timerContainer;
    private final TimerLabel timerLabel;

    public HeaderPanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.COLOR_HEADER_BG);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(30, 41, 59)),
                BorderFactory.createEmptyBorder(12, 24, 12, 24)
        ));
        setPreferredSize(new Dimension(1000, 60));

        // Brand / Title
        JPanel brandPanel = new JPanel();
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.X_AXIS));
        brandPanel.setOpaque(false);

        appTitleLabel = new JLabel(AppConfig.APP_NAME);
        appTitleLabel.setFont(UITheme.FONT_TITLE_LARGE);
        appTitleLabel.setForeground(UITheme.COLOR_HEADER_TEXT);

        JLabel subtitleLabel = new JLabel("  |  Online Examination Portal");
        subtitleLabel.setFont(UITheme.FONT_BODY);
        subtitleLabel.setForeground(new Color(148, 163, 184));

        brandPanel.add(appTitleLabel);
        brandPanel.add(subtitleLabel);
        add(brandPanel, BorderLayout.WEST);

        // Right side: Timer and User display
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.X_AXIS));
        rightPanel.setOpaque(false);

        // Timer Container
        timerContainer = new JPanel();
        timerContainer.setLayout(new BoxLayout(timerContainer, BoxLayout.X_AXIS));
        timerContainer.setOpaque(false);
        timerContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(51, 65, 85), 1),
                BorderFactory.createEmptyBorder(4, 12, 4, 12)
        ));

        JLabel timerIconLabel = new JLabel("Time Left: ");
        timerIconLabel.setFont(UITheme.FONT_BODY_BOLD);
        timerIconLabel.setForeground(Color.WHITE);

        timerLabel = new TimerLabel();

        timerContainer.add(timerIconLabel);
        timerContainer.add(timerLabel);
        timerContainer.setVisible(false); // only visible during exam

        // User Greeting
        userGreetingLabel = new JLabel();
        userGreetingLabel.setFont(UITheme.FONT_BODY_BOLD);
        userGreetingLabel.setForeground(new Color(226, 232, 240));
        userGreetingLabel.setVisible(false);

        rightPanel.add(timerContainer);
        rightPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        rightPanel.add(userGreetingLabel);

        add(rightPanel, BorderLayout.EAST);
    }

    public void setUser(User user) {
        if (user != null) {
            userGreetingLabel.setText("Student: " + user.getDisplayName());
            userGreetingLabel.setVisible(true);
        } else {
            userGreetingLabel.setText("");
            userGreetingLabel.setVisible(false);
        }
    }

    public void updateDisplayName(String displayName) {
        if (displayName != null && !displayName.trim().isEmpty()) {
            userGreetingLabel.setText("Student: " + displayName);
            userGreetingLabel.setVisible(true);
        }
    }

    public void showTimer(boolean visible) {
        timerContainer.setVisible(visible);
        if (!visible) {
            timerLabel.stopTimer();
        }
        revalidate();
        repaint();
    }

    public boolean isTimerVisible() {
        return timerContainer.isVisible();
    }

    public TimerLabel getTimerLabel() {
        return timerLabel;
    }
}
