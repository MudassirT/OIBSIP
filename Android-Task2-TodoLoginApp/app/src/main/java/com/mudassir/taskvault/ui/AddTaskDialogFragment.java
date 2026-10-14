package com.mudassir.taskvault.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.mudassir.taskvault.R;
import com.mudassir.taskvault.util.Validators;

/**
 * DialogFragment for adding a new task with validation and double-click protection.
 */
public class AddTaskDialogFragment extends DialogFragment {

    public static final String TAG = "AddTaskDialogFragment";

    public interface OnTaskCreatedListener {
        void onTaskCreated(String title, String notes);
    }

    private OnTaskCreatedListener listener;

    public static AddTaskDialogFragment newInstance() {
        return new AddTaskDialogFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnTaskCreatedListener) {
            listener = (OnTaskCreatedListener) context;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Context context = requireContext();
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_add_task, null);

        TextInputLayout tilTitle = view.findViewById(R.id.tilTaskTitle);
        TextInputEditText etTitle = view.findViewById(R.id.etTaskTitle);
        TextInputLayout tilNotes = view.findViewById(R.id.tilTaskNotes);
        TextInputEditText etNotes = view.findViewById(R.id.etTaskNotes);
        Button btnSave = view.findViewById(R.id.btnSaveTask);
        Button btnCancel = view.findViewById(R.id.btnCancelTask);

        AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setView(view)
                .setCancelable(true)
                .create();

        // Clear error on title input change
        etTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilTitle.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnCancel.setOnClickListener(v -> dismiss());

        btnSave.setOnClickListener(v -> {
            String rawTitle = etTitle.getText() != null ? etTitle.getText().toString() : "";
            String rawNotes = etNotes.getText() != null ? etNotes.getText().toString() : "";

            String trimmedTitle = rawTitle.trim();
            String trimmedNotes = rawNotes.trim();

            if (trimmedTitle.isEmpty()) {
                tilTitle.setError(getString(R.string.error_empty_task_title));
                etTitle.requestFocus();
                return;
            }

            if (trimmedTitle.length() > Validators.MAX_TASK_TITLE_LENGTH) {
                tilTitle.setError(getString(R.string.error_task_title_too_long));
                return;
            }

            if (trimmedNotes.length() > Validators.MAX_TASK_NOTES_LENGTH) {
                tilNotes.setError(getString(R.string.error_task_notes_too_long));
                return;
            }

            // Disable button to prevent double-submissions
            btnSave.setEnabled(false);

            if (listener != null) {
                listener.onTaskCreated(trimmedTitle, trimmedNotes);
            }
            dismiss();
        });

        return dialog;
    }
}
