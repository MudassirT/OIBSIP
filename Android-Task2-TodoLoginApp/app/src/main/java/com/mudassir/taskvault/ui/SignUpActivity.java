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
import com.mudassir.taskvault.util.PasswordHasher;
import com.mudassir.taskvault.util.Validators;

/**
 * Screen handling user account registration with validation and PBKDF2 hashing.
 */
public class SignUpActivity extends AppCompatActivity {

    private TextInputLayout tilName;
    private TextInputEditText etName;
    private TextInputLayout tilEmail;
    private TextInputEditText etEmail;
    private TextInputLayout tilPassword;
    private TextInputEditText etPassword;
    private TextInputLayout tilConfirmPassword;
    private TextInputEditText etConfirmPassword;
    private Button btnRegister;
    private TextView tvGoToLogin;

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        databaseHelper = DatabaseHelper.getInstance(this);
        initViews();
        setupListeners();
    }

    private void initViews() {
        tilName = findViewById(R.id.tilName);
        etName = findViewById(R.id.etName);
        tilEmail = findViewById(R.id.tilEmail);
        etEmail = findViewById(R.id.etEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etPassword = findViewById(R.id.etPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);
    }

    private void setupListeners() {
        etName.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilName.setError(null);
            }
        });

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

        etConfirmPassword.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilConfirmPassword.setError(null);
            }
        });

        btnRegister.setOnClickListener(v -> performRegistration());

        tvGoToLogin.setOnClickListener(v -> finish());
    }

    private void performRegistration() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString() : "";

        boolean hasError = false;

        // 1. Name validation
        if (!Validators.isValidName(name)) {
            tilName.setError(getString(R.string.error_empty_name));
            hasError = true;
        }

        // 2. Email validation
        if (email.isEmpty()) {
            tilEmail.setError(getString(R.string.error_empty_email));
            hasError = true;
        } else if (!Validators.isValidEmail(email)) {
            tilEmail.setError(getString(R.string.error_invalid_email));
            hasError = true;
        }

        // 3. Password length & character validation
        if (password.isEmpty()) {
            tilPassword.setError(getString(R.string.error_empty_password));
            hasError = true;
        } else if (!Validators.isPasswordLengthValid(password)) {
            tilPassword.setError(getString(R.string.error_password_too_short));
            hasError = true;
        } else if (!Validators.doesPasswordContainDigit(password)) {
            tilPassword.setError(getString(R.string.error_password_no_digit));
            hasError = true;
        }

        // 4. Confirm password validation
        if (confirmPassword.isEmpty()) {
            tilConfirmPassword.setError(getString(R.string.error_empty_confirm_password));
            hasError = true;
        } else if (!Validators.doPasswordsMatch(password, confirmPassword)) {
            tilConfirmPassword.setError(getString(R.string.error_password_mismatch));
            hasError = true;
        }

        if (hasError) {
            return;
        }

        // Disable button during background processing
        btnRegister.setEnabled(false);
        btnRegister.setText(R.string.btn_registering);

        databaseHelper.runAsync(() -> {
            // Check for duplicate email (case-insensitive)
            if (databaseHelper.isEmailTaken(email)) {
                return -2L; // Duplicate email code
            }

            // Cryptographically secure salt & PBKDF2 hash
            String salt = PasswordHasher.generateSalt();
            String hash = PasswordHasher.hashPassword(password, salt);

            return databaseHelper.registerUser(name, email, hash, salt);
        }, result -> {
            btnRegister.setEnabled(true);
            btnRegister.setText(R.string.btn_register);

            if (result == null || result == -1L) {
                Snackbar.make(findViewById(android.R.id.content),
                        R.string.error_registration_failed,
                        Snackbar.LENGTH_LONG).show();
            } else if (result == -2L) {
                tilEmail.setError(getString(R.string.error_email_already_exists));
                etEmail.requestFocus();
            } else {
                // Registration successful: return to login with confirmation
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                intent.putExtra(LoginActivity.EXTRA_REGISTRATION_SUCCESS, true);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });
    }

    private static abstract class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void afterTextChanged(Editable s) {}
    }
}
