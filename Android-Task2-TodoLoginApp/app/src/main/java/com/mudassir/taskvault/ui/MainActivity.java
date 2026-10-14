package com.mudassir.taskvault.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.mudassir.taskvault.R;
import com.mudassir.taskvault.data.DatabaseHelper;
import com.mudassir.taskvault.data.Task;
import com.mudassir.taskvault.util.SessionManager;

import java.util.List;

/**
 * Main dashboard screen displaying user-specific tasks with add, update,
 * complete, delete, and swipe-to-delete capabilities.
 */
public class MainActivity extends AppCompatActivity implements
        AddTaskDialogFragment.OnTaskCreatedListener,
        TaskAdapter.OnTaskStatusChangeListener,
        TaskAdapter.OnTaskDeleteClickListener {

    private MaterialToolbar toolbar;
    private TextView tvWelcomeUser;
    private TextView tvTaskSummary;
    private TextView tvBadgePending;
    private TextView tvBadgeCompleted;
    private RecyclerView rvTasks;
    private LinearLayout layoutEmptyState;
    private FloatingActionButton fabAddTask;

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private TaskAdapter taskAdapter;

    private long currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        // Security check: ensure active session
        if (!sessionManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        currentUserId = sessionManager.getUserId();
        databaseHelper = DatabaseHelper.getInstance(this);

        setContentView(R.layout.activity_main);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();
        setupSwipeToDelete();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!sessionManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }
        loadTasks();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvWelcomeUser = findViewById(R.id.tvWelcomeUser);
        tvTaskSummary = findViewById(R.id.tvTaskSummary);
        tvBadgePending = findViewById(R.id.tvBadgePending);
        tvBadgeCompleted = findViewById(R.id.tvBadgeCompleted);
        rvTasks = findViewById(R.id.rvTasks);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        fabAddTask = findViewById(R.id.fabAddTask);

        // Personalized welcome
        String userName = sessionManager.getUserName();
        tvWelcomeUser.setText(getString(R.string.welcome_user, userName));
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
    }

    private void setupRecyclerView() {
        taskAdapter = new TaskAdapter(this, this);
        rvTasks.setLayoutManager(new LinearLayoutManager(this));
        rvTasks.setAdapter(taskAdapter);
    }

    private void setupListeners() {
        fabAddTask.setOnClickListener(v -> showAddTaskDialog());
    }

    private void showAddTaskDialog() {
        AddTaskDialogFragment dialog = AddTaskDialogFragment.newInstance();
        dialog.show(getSupportFragmentManager(), AddTaskDialogFragment.TAG);
    }

    /**
     * Loads tasks belonging strictly to the currently authenticated user off the main thread.
     */
    private void loadTasks() {
        databaseHelper.runAsync(
                () -> databaseHelper.getTasksForUser(currentUserId),
                this::updateTaskUI
        );
    }

    private void updateTaskUI(List<Task> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            taskAdapter.setTasks(null);
            rvTasks.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);

            tvTaskSummary.setText(getString(R.string.task_count_summary, 0, 0));
            tvBadgePending.setText(getString(R.string.badge_pending, 0));
            tvBadgeCompleted.setText(getString(R.string.badge_completed, 0));
        } else {
            taskAdapter.setTasks(tasks);
            rvTasks.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);

            int pendingCount = 0;
            int completedCount = 0;
            for (Task task : tasks) {
                if (task.isCompleted()) {
                    completedCount++;
                } else {
                    pendingCount++;
                }
            }

            tvTaskSummary.setText(getString(R.string.task_count_summary, pendingCount, completedCount));
            tvBadgePending.setText(getString(R.string.badge_pending, pendingCount));
            tvBadgeCompleted.setText(getString(R.string.badge_completed, completedCount));
        }
    }

    // ==========================================
    // TASK CALLBACKS
    // ==========================================

    @Override
    public void onTaskCreated(String title, String notes) {
        databaseHelper.runAsync(
                () -> databaseHelper.addTask(currentUserId, title, notes),
                result -> {
                    if (result != null && result > 0) {
                        loadTasks();
                        Snackbar.make(findViewById(android.R.id.content),
                                R.string.msg_task_saved,
                                Snackbar.LENGTH_SHORT).show();
                    }
                }
        );
    }

    @Override
    public void onStatusChanged(Task task, boolean isCompleted) {
        databaseHelper.runAsync(
                () -> databaseHelper.setTaskCompleted(task.getId(), isCompleted),
                success -> {
                    // Refresh task order: moves completed tasks to bottom
                    loadTasks();
                }
        );
    }

    @Override
    public void onDeleteClicked(Task task) {
        showDeleteConfirmationDialog(task);
    }

    private void showDeleteConfirmationDialog(Task task) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(R.string.dialog_delete_message)
                .setPositiveButton(R.string.btn_delete, (dialog, which) -> {
                    databaseHelper.runAsync(
                            () -> databaseHelper.deleteTask(task.getId()),
                            success -> {
                                loadTasks();
                                showUndoSnackbar(task);
                            }
                    );
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    /**
     * Supports swipe-to-delete with interactive Undo Snackbar.
     */
    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Task taskToDelete = taskAdapter.getTaskAt(position);
                if (taskToDelete != null) {
                    databaseHelper.runAsync(
                            () -> databaseHelper.deleteTask(taskToDelete.getId()),
                            success -> {
                                loadTasks();
                                showUndoSnackbar(taskToDelete);
                            }
                    );
                }
            }
        };

        new ItemTouchHelper(simpleCallback).attachToRecyclerView(rvTasks);
    }

    private void showUndoSnackbar(Task deletedTask) {
        Snackbar snackbar = Snackbar.make(
                findViewById(android.R.id.content),
                R.string.task_deleted_message,
                Snackbar.LENGTH_LONG
        );
        snackbar.setAction(R.string.action_undo, v -> {
            databaseHelper.runAsync(
                    () -> databaseHelper.insertRestoredTask(deletedTask),
                    res -> loadTasks()
            );
        });
        snackbar.show();
    }

    // ==========================================
    // MENU & LOGOUT
    // ==========================================

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            showLogoutConfirmationDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showLogoutConfirmationDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_logout_title)
                .setMessage(R.string.dialog_logout_message)
                .setPositiveButton(R.string.menu_logout, (dialog, which) -> performLogout())
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void performLogout() {
        // Clear session and back stack
        sessionManager.clearSession();
        redirectToLogin();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
