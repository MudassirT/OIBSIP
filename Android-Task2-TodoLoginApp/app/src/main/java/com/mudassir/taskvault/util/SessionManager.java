package com.mudassir.taskvault.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages user session state using SharedPreferences.
 * Passwords, hashes, and salts are strictly excluded from storage.
 */
public class SessionManager {

    private static final String PREF_NAME = "taskvault_session";
    private static final String KEY_USER_ID = "key_user_id";
    private static final String KEY_USER_NAME = "key_user_name";
    private static final String KEY_USER_EMAIL = "key_user_email";
    private static final String KEY_IS_LOGGED_IN = "key_is_logged_in";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Stores user identity attributes in session after successful login.
     */
    public void createSession(long userId, String name, String email) {
        prefs.edit()
                .putLong(KEY_USER_ID, userId)
                .putString(KEY_USER_NAME, name)
                .putString(KEY_USER_EMAIL, email)
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .apply();
    }

    /**
     * Checks if a valid authenticated user session currently exists.
     */
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false) && getUserId() > 0;
    }

    /**
     * Retrieves the active user's ID.
     */
    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, -1);
    }

    /**
     * Retrieves the active user's display name.
     */
    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "User");
    }

    /**
     * Retrieves the active user's email address.
     */
    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "");
    }

    /**
     * Clears all session data upon logout.
     */
    public void clearSession() {
        prefs.edit().clear().apply();
    }
}
