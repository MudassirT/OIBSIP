package com.mudassir.examportal.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Unified UI styling tokens, color palette, typography, and component factory helpers.
 */
public final class UITheme {

    private UITheme() {
    }

    // Color Palette
    public static final Color COLOR_APP_BG = new Color(248, 250, 252);
    public static final Color COLOR_CARD_BG = Color.WHITE;
    public static final Color COLOR_BORDER = new Color(226, 232, 240);
    public static final Color COLOR_HEADER_BG = new Color(15, 23, 42); // Deep slate
    public static final Color COLOR_HEADER_TEXT = Color.WHITE;

    public static final Color COLOR_PRIMARY = new Color(37, 99, 235); // Blue 600
    public static final Color COLOR_PRIMARY_HOVER = new Color(29, 78, 216);
    public static final Color COLOR_SECONDARY = new Color(100, 116, 139); // Slate 500
    public static final Color COLOR_SECONDARY_BG = new Color(241, 245, 249); // Slate 100

    public static final Color COLOR_TEXT_PRIMARY = new Color(15, 23, 42);
    public static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);

    // Green/Red reserved for status & timer
    public static final Color COLOR_STATUS_CORRECT = new Color(16, 185, 129);   // Emerald
    public static final Color COLOR_STATUS_INCORRECT = new Color(239, 68, 68);  // Rose Red
    public static final Color COLOR_STATUS_UNANSWERED = new Color(148, 163, 184); // Slate 400
    public static final Color COLOR_REVIEW_PURPLE = new Color(139, 92, 246);   // Violet

    public static final Color COLOR_TIMER_NORMAL = new Color(16, 185, 129);
    public static final Color COLOR_TIMER_AMBER = new Color(217, 119, 6);
    public static final Color COLOR_TIMER_RED = new Color(220, 38, 38);

    // Fonts
    public static final Font FONT_TITLE_LARGE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_TITLE_MEDIUM = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_QUESTION = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_TIMER = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_BADGE = new Font("Segoe UI", Font.BOLD, 11);

    /**
     * Creates a styled card container panel with clean borders and background.
     */
    public static JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(COLOR_CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                BorderFactory.createEmptyBorder(16, 20, 16, 20)
        ));
        return panel;
    }

    /**
     * Creates a primary action button with standard colors and padding.
     */
    public static JButton createPrimaryButton(String text, char mnemonic) {
        JButton button = new JButton(text);
        button.setFont(FONT_BODY_BOLD);
        button.setBackground(COLOR_PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        if (mnemonic != '\0') {
            button.setMnemonic(mnemonic);
        }
        return button;
    }

    /**
     * Creates a secondary/neutral button.
     */
    public static JButton createSecondaryButton(String text, char mnemonic) {
        JButton button = new JButton(text);
        button.setFont(FONT_BODY_BOLD);
        button.setBackground(COLOR_SECONDARY_BG);
        button.setForeground(COLOR_TEXT_PRIMARY);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                BorderFactory.createEmptyBorder(9, 16, 9, 16)
        ));
        if (mnemonic != '\0') {
            button.setMnemonic(mnemonic);
        }
        return button;
    }

    /**
     * Creates a danger/caution button.
     */
    public static JButton createDangerButton(String text, char mnemonic) {
        JButton button = new JButton(text);
        button.setFont(FONT_BODY_BOLD);
        button.setBackground(new Color(254, 242, 242));
        button.setForeground(COLOR_STATUS_INCORRECT);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(252, 165, 165), 1),
                BorderFactory.createEmptyBorder(9, 16, 9, 16)
        ));
        if (mnemonic != '\0') {
            button.setMnemonic(mnemonic);
        }
        return button;
    }
}
