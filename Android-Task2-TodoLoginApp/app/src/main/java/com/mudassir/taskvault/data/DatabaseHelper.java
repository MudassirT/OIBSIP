package com.mudassir.taskvault.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * SQLite database helper managing TaskVault's local encrypted storage.
 * Enforces foreign key constraints and parameterised queries for SQL injection safety.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "taskvault.db";
    private static final int DATABASE_VERSION = 1;

    // Table: users
    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "id";
    public static final String COL_USER_NAME = "name";
    public static final String COL_USER_EMAIL = "email";
    public static final String COL_USER_PASSWORD_HASH = "password_hash";
    public static final String COL_USER_SALT = "salt";
    public static final String COL_USER_CREATED_AT = "created_at";

    // Table: tasks
    public static final String TABLE_TASKS = "tasks";
    public static final String COL_TASK_ID = "id";
    public static final String COL_TASK_USER_ID = "user_id";
    public static final String COL_TASK_TITLE = "title";
    public static final String COL_TASK_NOTES = "notes";
    public static final String COL_TASK_IS_COMPLETED = "is_completed";
    public static final String COL_TASK_CREATED_AT = "created_at";
    public static final String COL_TASK_COMPLETED_AT = "completed_at";

    // SQL creation statements
    private static final String CREATE_TABLE_USERS =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_USER_NAME + " TEXT NOT NULL, " +
                    COL_USER_EMAIL + " TEXT NOT NULL UNIQUE, " +
                    COL_USER_PASSWORD_HASH + " TEXT NOT NULL, " +
                    COL_USER_SALT + " TEXT NOT NULL, " +
                    COL_USER_CREATED_AT + " INTEGER NOT NULL" +
                    ");";

    private static final String CREATE_TABLE_TASKS =
            "CREATE TABLE " + TABLE_TASKS + " (" +
                    COL_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_TASK_USER_ID + " INTEGER NOT NULL, " +
                    COL_TASK_TITLE + " TEXT NOT NULL, " +
                    COL_TASK_NOTES + " TEXT, " +
                    COL_TASK_IS_COMPLETED + " INTEGER DEFAULT 0, " +
                    COL_TASK_CREATED_AT + " INTEGER NOT NULL, " +
                    COL_TASK_COMPLETED_AT + " INTEGER DEFAULT 0, " +
                    "FOREIGN KEY (" + COL_TASK_USER_ID + ") REFERENCES " +
                    TABLE_USERS + " (" + COL_USER_ID + ") ON DELETE CASCADE" +
                    ");";

    // Asynchronous worker pool
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // Enable SQLite foreign key constraints and cascade actions
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_TASKS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Upgrade migration strategy
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // ==========================================
    // USER OPERATIONS
    // ==========================================

    /**
     * Registers a new user with salt and password hash.
     * Uses parameterised ContentValues to prevent SQL injection.
     *
     * @return Row ID of created user, or -1 on duplicate/error.
     */
    public long registerUser(String name, String email, String passwordHash, String salt) {
        if (name == null || email == null || passwordHash == null || salt == null) {
            return -1;
        }

        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_NAME, name.trim());
        values.put(COL_USER_EMAIL, email.trim().toLowerCase());
        values.put(COL_USER_PASSWORD_HASH, passwordHash);
        values.put(COL_USER_SALT, salt);
        values.put(COL_USER_CREATED_AT, System.currentTimeMillis());

        try {
            return db.insert(TABLE_USERS, null, values);
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Finds a user by their lowercase email address.
     * Uses parameterised query with selectionArgs.
     */
    public User findUserByEmail(String email) {
        if (email == null) {
            return null;
        }

        SQLiteDatabase db = getReadableDatabase();
        String normalizedEmail = email.trim().toLowerCase();
        String selection = COL_USER_EMAIL + " = ?";
        String[] selectionArgs = { normalizedEmail };

        try (Cursor cursor = db.query(
                TABLE_USERS,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_USER_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_NAME));
                String dbEmail = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_EMAIL));
                String hash = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_PASSWORD_HASH));
                String salt = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_SALT));
                long createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(COL_USER_CREATED_AT));
                return new User(id, name, dbEmail, hash, salt, createdAt);
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    /**
     * Checks if a user already exists with the given email address.
     */
    public boolean isEmailTaken(String email) {
        return findUserByEmail(email) != null;
    }

    /**
     * Retrieves user profile details by ID.
     */
    public User getUserById(long userId) {
        SQLiteDatabase db = getReadableDatabase();
        String selection = COL_USER_ID + " = ?";
        String[] selectionArgs = { String.valueOf(userId) };

        try (Cursor cursor = db.query(TABLE_USERS, null, selection, selectionArgs, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_USER_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_NAME));
                String email = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_EMAIL));
                String hash = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_PASSWORD_HASH));
                String salt = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_SALT));
                long createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(COL_USER_CREATED_AT));
                return new User(id, name, email, hash, salt, createdAt);
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    // ==========================================
    // TASK OPERATIONS
    // ==========================================

    /**
     * Inserts a new task belonging to a specific user.
     */
    public long addTask(long userId, String title, String notes) {
        if (title == null || title.trim().isEmpty()) {
            return -1;
        }

        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TASK_USER_ID, userId);
        values.put(COL_TASK_TITLE, title.trim());
        values.put(COL_TASK_NOTES, notes != null ? notes.trim() : "");
        values.put(COL_TASK_IS_COMPLETED, 0);
        values.put(COL_TASK_CREATED_AT, System.currentTimeMillis());
        values.put(COL_TASK_COMPLETED_AT, 0);

        try {
            return db.insert(TABLE_TASKS, null, values);
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Fetches all tasks strictly belonging to the given user ID.
     * Orders pending tasks first (newest to oldest), then completed tasks (newest to oldest).
     */
    public List<Task> getTasksForUser(long userId) {
        List<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        String selection = COL_TASK_USER_ID + " = ?";
        String[] selectionArgs = { String.valueOf(userId) };
        String orderBy = COL_TASK_IS_COMPLETED + " ASC, " + COL_TASK_CREATED_AT + " DESC";

        try (Cursor cursor = db.query(
                TABLE_TASKS,
                null,
                selection,
                selectionArgs,
                null,
                null,
                orderBy
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_TASK_ID));
                    long uId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_TASK_USER_ID));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_TITLE));
                    String notes = cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_NOTES));
                    int isCompletedInt = cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_IS_COMPLETED));
                    long createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(COL_TASK_CREATED_AT));
                    long completedAt = cursor.getLong(cursor.getColumnIndexOrThrow(COL_TASK_COMPLETED_AT));

                    tasks.add(new Task(id, uId, title, notes, isCompletedInt == 1, createdAt, completedAt));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            return tasks;
        }
        return tasks;
    }

    /**
     * Updates completion status of a task.
     */
    public boolean setTaskCompleted(long taskId, boolean isCompleted) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TASK_IS_COMPLETED, isCompleted ? 1 : 0);
        values.put(COL_TASK_COMPLETED_AT, isCompleted ? System.currentTimeMillis() : 0);

        String whereClause = COL_TASK_ID + " = ?";
        String[] whereArgs = { String.valueOf(taskId) };

        return db.update(TABLE_TASKS, values, whereClause, whereArgs) > 0;
    }

    /**
     * Updates title and notes of a task.
     */
    public boolean updateTask(long taskId, String title, String notes) {
        if (title == null || title.trim().isEmpty()) {
            return false;
        }

        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TASK_TITLE, title.trim());
        values.put(COL_TASK_NOTES, notes != null ? notes.trim() : "");

        String whereClause = COL_TASK_ID + " = ?";
        String[] whereArgs = { String.valueOf(taskId) };

        return db.update(TABLE_TASKS, values, whereClause, whereArgs) > 0;
    }

    /**
     * Permanently deletes a task by ID.
     */
    public boolean deleteTask(long taskId) {
        SQLiteDatabase db = getWritableDatabase();
        String whereClause = COL_TASK_ID + " = ?";
        String[] whereArgs = { String.valueOf(taskId) };

        return db.delete(TABLE_TASKS, whereClause, whereArgs) > 0;
    }

    /**
     * Re-inserts a task with a given timestamp (useful for Undo functionality).
     */
    public long insertRestoredTask(Task task) {
        if (task == null) return -1;
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TASK_USER_ID, task.getUserId());
        values.put(COL_TASK_TITLE, task.getTitle());
        values.put(COL_TASK_NOTES, task.getNotes());
        values.put(COL_TASK_IS_COMPLETED, task.isCompleted() ? 1 : 0);
        values.put(COL_TASK_CREATED_AT, task.getCreatedAt());
        values.put(COL_TASK_COMPLETED_AT, task.getCompletedAt());
        return db.insert(TABLE_TASKS, null, values);
    }

    // ==========================================
    // ASYNC EXECUTOR HELPER
    // ==========================================

    public interface DbCallback<T> {
        void onResult(T result);
    }

    public <T> void runAsync(java.util.concurrent.Callable<T> callable, DbCallback<T> callback) {
        executorService.execute(() -> {
            try {
                T result = callable.call();
                mainHandler.post(() -> {
                    if (callback != null) {
                        callback.onResult(result);
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (callback != null) {
                        callback.onResult(null);
                    }
                });
            }
        });
    }
}
