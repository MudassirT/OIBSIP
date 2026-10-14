package com.mudassir.taskvault.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.mudassir.taskvault.R;
import com.mudassir.taskvault.data.DatabaseHelper;
import com.mudassir.taskvault.data.User;
import com.mudassir.taskvault.util.PasswordHasher;
import com.mudassir.taskvault.util.SessionManager;

/**
 * Authentication screen handling user login and session restoration.
 */
public class LoginActivity extends AppCompatActivity {

    public static final String EXTRA_REGISTRATION_SUCCESS = "extra_registration_success";

    private TextInputLayout tilEmail;
    private TextInputEditText etEmail;
    private TextInputLayout tilPassword;
    private TextInputEditText etPassword;
    private Button btnLogin;
    private TextView tvGoToSignUp;

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        // Session check: If user already authenticated, navigate directly to task list
        if (sessionManager.isLoggedIn()) {
            navigateToMainActivity();
            return;
        }

        setContentView(R.layout.activity_login);

        databaseHelper = DatabaseHelper.getInstance(this);
        initViews();
        setupListeners();
        checkForRegistrationMessage();
    }

    private void initViews() {
        tilEmail = findViewById(R.id.tilEmail);
        etEmail = findViewById(R.id.etEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoToSignUp = findViewById(R.id.tvGoToSignUp);
    }

    private void setupListeners() {
        // Clear errors upon typing
        etEmail.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilEmail.setError(null);
            }
        });

        etPassword.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilPassword.setError(null);
            }
        });

        btnLogin.setOnClickListener(v -> performLogin());

        tvGoToSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }

    private void checkForRegistrationMessage() {
        if (getIntent().getBooleanExtra(EXTRA_REGISTRATION_SUCCESS, false)) {
            Snackbar.make(findViewById(android.R.id.content),
                    R.string.msg_signup_success,
                    Snackbar.LENGTH_LONG).show();
        }
    }

    private void performLogin() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";

        boolean hasError = false;

        if (email.isEmpty()) {
            tilEmail.setError(getString(R.string.error_empty_login_identifier));
            hasError = true;
        }

        if (password.isEmpty()) {
            tilPassword.setError(getString(R.string.error_empty_password));
            hasError = true;
        }

        if (hasError) {
            return;
        }

        // Prevent double taps while background authentication runs
        btnLogin.setEnabled(false);
        btnLogin.setText(R.string.btn_logging_in);

        databaseHelper.runAsync(() -> {
            User user = databaseHelper.findUserByEmail(email);
            if (user == null) {
                return null;
            }

            boolean isMatch = PasswordHasher.verifyPassword(password, user.getPasswordHash(), user.getSalt());
            return isMatch ? user : null;
        }, user -> {
            btnLogin.setEnabled(true);
            btnLogin.setText(R.string.btn_login);

            if (user != null) {
                // Successful authentication: save session without storing password/hash
                sessionManager.createSession(user.getId(), user.getName(), user.getEmail());
                navigateToMainActivity();
            } else {
                // Generic failure message for security: never disclose if email or password was wrong
                tilEmail.setError(getString(R.string.error_invalid_credentials));
                tilPassword.setError(getString(R.string.error_invalid_credentials));
            }
        });
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private static abstract class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void afterTextChanged(Editable s) {}
    }
}
