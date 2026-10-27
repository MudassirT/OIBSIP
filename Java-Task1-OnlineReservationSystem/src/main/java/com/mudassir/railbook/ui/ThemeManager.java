package com.mudassir.railbook.ui;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages modern UI themes, color palettes, fonts, and button styles for RailBook.
 * Employs FlatLaf with graceful fallback to system Look and Feel.
 *
 * @author Mudassir Ahmed
 */
public final class ThemeManager {

    private static final Logger LOGGER = Logger.getLogger(ThemeManager.class.getName());

    // Color Palette
    public static final Color COLOR_PRIMARY = new Color(30, 64, 175); // Deep Indigo
    public static final Color COLOR_PRIMARY_HOVER = new Color(29, 78, 216);
    public static final Color COLOR_ACCENT = new Color(13, 148, 136); // Teal Accent
    public static final Color COLOR_SUCCESS = new Color(22, 101, 52); // Forest Green
    public static final Color COLOR_DANGER = new Color(220, 38, 38); // Crimson Red
    public static final Color COLOR_DANGER_HOVER = new Color(185, 28, 28);
    public static final Color COLOR_TEXT_DARK = new Color(15, 23, 42); // Slate 900
    public static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139); // Slate 500
    public static final Color COLOR_CARD_BG = new Color(248, 250, 252); // Slate 50
    public static final Color COLOR_BORDER = new Color(226, 232, 240); // Slate 200

    // Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_PNR = new Font("Consolas", Font.BOLD, 20);

    private ThemeManager() {
        // Utility class
    }

    /**
     * Initializes the application Look and Feel.
     */
    public static void initializeLookAndFeel() {
        try {
            FlatLightLaf.setup();
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("ProgressBar.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ScrollBar.thumbArc", 8);
            LOGGER.info("FlatLaf Light theme initialized successfully.");
        } catch (Throwable t) {
            LOGGER.log(Level.WARNING, "FlatLaf not available, falling back to system Look and Feel", t);
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Failed to initialize system Look and Feel", ex);
            }
        }
    }

    /**
     * Styles a primary call-to-action button.
     */
    public static void stylePrimaryButton(JButton button) {
        button.setBackground(COLOR_PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFont(FONT_BOLD);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(button.getPreferredSize().width, 36));
    }

    /**
     * Styles a danger action button (e.g. cancellation).
     */
    public static void styleDangerButton(JButton button) {
        button.setBackground(COLOR_DANGER);
        button.setForeground(Color.WHITE);
        button.setFont(FONT_BOLD);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(button.getPreferredSize().width, 36));
    }

    /**
     * Styles a secondary / neutral button.
     */
    public static void styleSecondaryButton(JButton button) {
        button.setBackground(Color.WHITE);
        button.setForeground(COLOR_TEXT_DARK);
        button.setFont(FONT_REGULAR);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)
        ));
        button.setPreferredSize(new Dimension(button.getPreferredSize().width, 36));
    }

    /**
     * Creates an elegant card border with padding.
     */
    public static Border createCardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                BorderFactory.createEmptyBorder(16, 20, 16, 20)
        );
    }
}
