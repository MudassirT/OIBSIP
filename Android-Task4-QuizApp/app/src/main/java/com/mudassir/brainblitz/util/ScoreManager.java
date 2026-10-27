package com.mudassir.brainblitz.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Utility manager for handling persistent high scores and quiz statistics via SharedPreferences.
 */
public class ScoreManager {

    private final SharedPreferences prefs;

    public ScoreManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Retrieves the highest score achieved. Returns -1 if no games have been completed yet.
     */
    public int getBestScore() {
        return prefs.getInt(Constants.KEY_BEST_SCORE, -1);
    }

    /**
     * Retrieves the best percentage achieved. Returns 0 if none.
     */
    public int getBestPercentage() {
        return prefs.getInt(Constants.KEY_BEST_PERCENTAGE, 0);
    }

    /**
     * Retrieves the total count of completed quizzes.
     */
    public int getTotalGamesPlayed() {
        return prefs.getInt(Constants.KEY_TOTAL_GAMES, 0);
    }

    /**
     * Checks if this score is a new high score and updates persistent storage.
     * @return true if a new high score was set, false otherwise.
     */
    public boolean recordQuizResult(int score, int totalQuestions) {
        int previousBest = getBestScore();
        int totalGames = getTotalGamesPlayed() + 1;
        int percentage = totalQuestions > 0 ? (int) Math.round(((double) score / totalQuestions) * 100) : 0;

        boolean isNewHigh = (score > previousBest);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(Constants.KEY_TOTAL_GAMES, totalGames);

        if (isNewHigh || previousBest == -1) {
            editor.putInt(Constants.KEY_BEST_SCORE, score);
            editor.putInt(Constants.KEY_BEST_PERCENTAGE, percentage);
            editor.apply();
            return previousBest != -1; // If it was -1, it's their very first game
        }

        editor.apply();
        return false;
    }

    /**
     * Clears all stored scores. Useful for testing and user resets.
     */
    public void resetScores() {
        prefs.edit().clear().apply();
    }
}
