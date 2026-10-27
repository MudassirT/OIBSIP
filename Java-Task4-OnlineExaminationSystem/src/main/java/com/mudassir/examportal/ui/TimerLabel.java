package com.mudassir.examportal.ui;

import com.mudassir.examportal.config.AppConfig;
import com.mudassir.examportal.service.ExamSession;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import java.awt.Color;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Reusable countdown timer label that calculates remaining time against a fixed target Instant
 * to avoid drift, dynamically updates color thresholds, and safely executes an auto-submit callback at zero.
 */
public class TimerLabel extends JLabel {

    private Timer swingTimer;
    private Instant targetEndInstant;
    private Runnable onTimeExpired;
    private final AtomicBoolean expiredTriggered = new AtomicBoolean(false);

    public TimerLabel() {
        super("00:00", SwingConstants.CENTER);
        setFont(UITheme.FONT_TIMER);
        setForeground(UITheme.COLOR_TIMER_NORMAL);
        setOpaque(false);
    }

    /**
     * Starts the countdown timer for the given duration in seconds.
     *
     * @param durationSeconds total duration
     * @param onTimeExpired   callback executed when timer reaches zero
     */
    public synchronized void startTimer(int durationSeconds, Runnable onTimeExpired) {
        stopTimer();
        this.onTimeExpired = onTimeExpired;
        this.expiredTriggered.set(false);
        this.targetEndInstant = Instant.now().plusSeconds(durationSeconds);

        updateDisplay();

        this.swingTimer = new Timer(AppConfig.TIMER_TICK_INTERVAL_MS, e -> updateDisplay());
        this.swingTimer.setRepeats(true);
        this.swingTimer.start();
    }

    /**
     * Stops the timer and cancels ticks.
     */
    public synchronized void stopTimer() {
        if (swingTimer != null) {
            swingTimer.stop();
            swingTimer = null;
        }
    }

    /**
     * Updates label text and color based on exact remaining time from the target Instant.
     */
    private void updateDisplay() {
        if (targetEndInstant == null) return;

        Instant now = Instant.now();
        long remainingSeconds = Duration.between(now, targetEndInstant).getSeconds();

        if (remainingSeconds <= 0) {
            setText("00:00");
            setForeground(UITheme.COLOR_TIMER_RED);
            stopTimer();

            // Fire auto-submit callback exactly once
            if (expiredTriggered.compareAndSet(false, true) && onTimeExpired != null) {
                onTimeExpired.run();
            }
            return;
        }

        setText(ExamSession.formatDuration(remainingSeconds));

        // Color coding: Red <= 60s, Amber <= 300s, Normal otherwise
        if (remainingSeconds <= AppConfig.TIMER_RED_THRESHOLD_SECONDS) {
            setForeground(UITheme.COLOR_TIMER_RED);
        } else if (remainingSeconds <= AppConfig.TIMER_AMBER_THRESHOLD_SECONDS) {
            setForeground(UITheme.COLOR_TIMER_AMBER);
        } else {
            setForeground(UITheme.COLOR_TIMER_NORMAL);
        }
    }

    /**
     * Helper to set custom text and color directly (useful for tests or demo screenshots).
     */
    public void setDisplay(String text, Color color) {
        setText(text);
        setForeground(color);
    }
}
