package com.mudassir.unitswitch.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;
import com.mudassir.unitswitch.R;
import com.mudassir.unitswitch.databinding.ActivityMainBinding;
import com.mudassir.unitswitch.logic.Category;
import com.mudassir.unitswitch.logic.ConversionResult;
import com.mudassir.unitswitch.logic.UnitConverter;
import com.mudassir.unitswitch.logic.UnitModel;
import com.mudassir.unitswitch.util.Constants;
import com.mudassir.unitswitch.util.FormatUtils;
import com.mudassir.unitswitch.util.ValidationResult;
import com.mudassir.unitswitch.util.ValidationUtils;

import java.util.List;

/**
 * Main Activity for UnitSwitch.
 * Provides user interaction for measurement categories, unit selections,
 * live and manual conversions, unit swapping, result copying, and full state restoration.
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private final UnitConverter unitConverter = new UnitConverter();

    private Category currentCategory = Category.LENGTH;
    private boolean isUpdatingSpinners = false;
    private long lastConvertClickTime = 0;
    private static final long DEBOUNCE_DELAY_MS = 350;

    // Cache of current results for clipboard copy and rotation preservation
    private String currentEquationText = "";
    private String currentResultLargeText = "";
    private String currentFormulaText = "";
    private boolean isResultVisible = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Restore saved preference for last category if not restoring from savedInstanceState
        if (savedInstanceState == null) {
            restoreLastUsedCategory();
        }

        setupCategoryChips();
        setupInputListeners();
        setupActionButtons();

        // Restore or initialize UI state
        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        } else {
            selectCategory(currentCategory, 0, 1, false);
        }
    }

    /**
     * Initializes listeners for the Material ChipGroup category selector.
     */
    private void setupCategoryChips() {
        binding.chipGroupCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;

            int checkedId = checkedIds.get(0);
            Category newCategory = getCategoryForChipId(checkedId);

            if (newCategory != currentCategory) {
                currentCategory = newCategory;
                saveLastUsedCategory(currentCategory);
                // Switching category resets both spinners, clears old result and errors
                selectCategory(currentCategory, 0, 1, true);
            }
        });
    }

    /**
     * Maps Chip view IDs to their corresponding Category enum constants.
     */
    @NonNull
    private Category getCategoryForChipId(int chipId) {
        if (chipId == R.id.chipWeight) return Category.WEIGHT;
        if (chipId == R.id.chipTemperature) return Category.TEMPERATURE;
        if (chipId == R.id.chipVolume) return Category.VOLUME;
        if (chipId == R.id.chipSpeed) return Category.SPEED;
        return Category.LENGTH;
    }

    /**
     * Maps Category enum constants to their corresponding Chip view IDs.
     */
    private int getChipIdForCategory(@NonNull Category category) {
        switch (category) {
            case WEIGHT:
                return R.id.chipWeight;
            case TEMPERATURE:
                return R.id.chipTemperature;
            case VOLUME:
                return R.id.chipVolume;
            case SPEED:
                return R.id.chipSpeed;
            case LENGTH:
            default:
                return R.id.chipLength;
        }
    }

    /**
     * Updates Spinners with units from the selected category and resets/refreshes state.
     */
    private void selectCategory(@NonNull Category category, int sourceIndex, int targetIndex, boolean isCategorySwitch) {
        isUpdatingSpinners = true;

        if (isCategorySwitch) {
            // Clear result and inline error upon switching category
            clearResult();
            binding.textInputLayoutValue.setError(null);
            binding.textInputLayoutValue.setErrorEnabled(false);
        }

        List<UnitModel> units = category.getUnits();
        UnitSpinnerAdapter sourceAdapter = new UnitSpinnerAdapter(this, units);
        UnitSpinnerAdapter targetAdapter = new UnitSpinnerAdapter(this, units);

        binding.spinnerSourceUnit.setAdapter(sourceAdapter);
        binding.spinnerTargetUnit.setAdapter(targetAdapter);

        int safeSourceIndex = Math.min(Math.max(sourceIndex, 0), units.size() - 1);
        int safeTargetIndex = Math.min(Math.max(targetIndex, 0), units.size() - 1);

        binding.spinnerSourceUnit.setSelection(safeSourceIndex, false);
        binding.spinnerTargetUnit.setSelection(safeTargetIndex, false);

        isUpdatingSpinners = false;

        // If live conversion is enabled and valid input exists, calculate immediately
        if (binding.switchLiveConversion.isChecked()) {
            performLiveConversion();
        }
    }

    /**
     * Configures input field listeners (live text watcher, keyboard IME action, spinner selection).
     */
    private void setupInputListeners() {
        // Live text change listener
        binding.editTextValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after) {
                // Clear any prior error as user resumes typing
                if (binding.textInputLayoutValue.isErrorEnabled()) {
                    binding.textInputLayoutValue.setError(null);
                    binding.textInputLayoutValue.setErrorEnabled(false);
                }

                if (binding.switchLiveConversion.isChecked()) {
                    performLiveConversion();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Soft keyboard 'Done' action listener
        binding.editTextValue.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard();
                performExplicitConversion();
                return true;
            }
            return false;
        });

        // Spinner item selection listener
        AdapterView.OnItemSelectedListener spinnerListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isUpdatingSpinners && binding.switchLiveConversion.isChecked()) {
                    performLiveConversion();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        binding.spinnerSourceUnit.setOnItemSelectedListener(spinnerListener);
        binding.spinnerTargetUnit.setOnItemSelectedListener(spinnerListener);

        // Live Conversion switch toggle
        binding.switchLiveConversion.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                performLiveConversion();
            }
        });
    }

    /**
     * Configures button click listeners (Convert, Swap, Copy).
     */
    private void setupActionButtons() {
        // Convert Button with debounce against rapid taps
        binding.btnConvert.setOnClickListener(v -> {
            long currentTime = SystemClock.elapsedRealtime();
            if (currentTime - lastConvertClickTime < DEBOUNCE_DELAY_MS) {
                return;
            }
            lastConvertClickTime = currentTime;
            hideKeyboard();
            performExplicitConversion();
        });

        // Swap Units Button with 180° rotation micro-animation
        binding.btnSwapUnits.setOnClickListener(v -> {
            // Animate icon rotation
            v.animate()
                    .rotationBy(180f)
                    .setDuration(250)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();

            int sourcePos = binding.spinnerSourceUnit.getSelectedItemPosition();
            int targetPos = binding.spinnerTargetUnit.getSelectedItemPosition();

            isUpdatingSpinners = true;
            binding.spinnerSourceUnit.setSelection(targetPos, false);
            binding.spinnerTargetUnit.setSelection(sourcePos, false);
            isUpdatingSpinners = false;

            // Re-convert if valid input exists or if a result was previously shown
            if (isResultVisible || binding.switchLiveConversion.isChecked()) {
                performLiveConversion();
            }
        });

        // Copy Result Button
        binding.btnCopyResult.setOnClickListener(v -> copyResultToClipboard());
    }

    /**
     * Explicit conversion triggered by pressing Convert or Done on the keyboard.
     * Shows Toasts and inline TextInputLayout errors on invalid or empty input.
     */
    private void performExplicitConversion() {
        String rawInput = binding.editTextValue.getText() != null
                ? binding.editTextValue.getText().toString()
                : "";

        UnitModel sourceUnit = (UnitModel) binding.spinnerSourceUnit.getSelectedItem();
        UnitModel targetUnit = (UnitModel) binding.spinnerTargetUnit.getSelectedItem();

        if (sourceUnit == null || targetUnit == null) return;

        ValidationResult validation = ValidationUtils.validateInput(rawInput, currentCategory, sourceUnit);

        if (!validation.isValid()) {
            String errorMsg = validation.getErrorMessage() != null
                    ? validation.getErrorMessage()
                    : getString(R.string.toast_empty_or_invalid);

            // Set inline error on TextInputLayout
            binding.textInputLayoutValue.setError(errorMsg);
            binding.textInputLayoutValue.setErrorEnabled(true);

            // Show Toast feedback
            if (validation.isBelowAbsoluteZero()) {
                Toast.makeText(this, R.string.toast_below_absolute_zero, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.toast_empty_or_invalid, Toast.LENGTH_SHORT).show();
            }

            clearResult();
            return;
        }

        // Input is valid: clear any prior error and calculate
        binding.textInputLayoutValue.setError(null);
        binding.textInputLayoutValue.setErrorEnabled(false);

        executeConversion(validation.getValue(), sourceUnit, targetUnit);
    }

    /**
     * Live conversion performed continuously during typing or unit switching.
     * Does NOT display intrusive errors when the input field is simply empty or partial.
     */
    private void performLiveConversion() {
        String rawInput = binding.editTextValue.getText() != null
                ? binding.editTextValue.getText().toString()
                : "";

        UnitModel sourceUnit = (UnitModel) binding.spinnerSourceUnit.getSelectedItem();
        UnitModel targetUnit = (UnitModel) binding.spinnerTargetUnit.getSelectedItem();

        if (sourceUnit == null || targetUnit == null) return;

        ValidationResult validation = ValidationUtils.validateInput(rawInput, currentCategory, sourceUnit);

        if (validation.isValid()) {
            binding.textInputLayoutValue.setError(null);
            binding.textInputLayoutValue.setErrorEnabled(false);
            executeConversion(validation.getValue(), sourceUnit, targetUnit);
        } else {
            // During live typing, silently revert to placeholder without flashing red error
            clearResult();
        }
    }

    /**
     * Executes the conversion computation and displays the result with subtle animation.
     */
    private void executeConversion(double inputValue, @NonNull UnitModel sourceUnit, @NonNull UnitModel targetUnit) {
        ConversionResult result = unitConverter.convert(currentCategory, sourceUnit, targetUnit, inputValue);

        if (!result.isSuccess()) {
            String errorMsg = result.getErrorMessage() != null
                    ? result.getErrorMessage()
                    : getString(R.string.error_invalid_number);
            binding.textInputLayoutValue.setError(errorMsg);
            binding.textInputLayoutValue.setErrorEnabled(true);
            clearResult();
            return;
        }

        double convertedValue = result.getResultValue();

        // Format outputs
        currentResultLargeText = FormatUtils.formatNumber(convertedValue) + " " + targetUnit.getSymbol();
        currentEquationText = FormatUtils.formatEquation(
                inputValue,
                sourceUnit.getSymbol(),
                convertedValue,
                targetUnit.getSymbol()
        );

        // Build conversion factor / rate note
        if (sourceUnit.equals(targetUnit)) {
            currentFormulaText = getString(R.string.label_formula_format, "1 " + sourceUnit.getSymbol() + " = 1 " + targetUnit.getSymbol());
        } else if (!currentCategory.isTemperature()) {
            double oneUnitRate = (1.0 * sourceUnit.getFactorToBase()) / targetUnit.getFactorToBase();
            currentFormulaText = "Rate: 1 " + sourceUnit.getSymbol() + " = " + FormatUtils.formatNumber(oneUnitRate) + " " + targetUnit.getSymbol();
        } else {
            currentFormulaText = getTemperatureFormulaDescription(sourceUnit.getId(), targetUnit.getId());
        }

        displayResult(currentResultLargeText, currentEquationText, currentFormulaText);
    }

    /**
     * Displays result components with a subtle fade-in animation.
     */
    private void displayResult(@NonNull String largeText, @NonNull String equationText, @NonNull String formulaText) {
        binding.tvResultLarge.setText(largeText);
        binding.tvResultEquation.setText(equationText);
        binding.tvResultFormula.setText(formulaText);

        if (!isResultVisible) {
            binding.layoutPlaceholder.setVisibility(View.GONE);
            binding.layoutResultContent.setAlpha(0f);
            binding.layoutResultContent.setVisibility(View.VISIBLE);
            binding.layoutResultContent.animate()
                    .alpha(1f)
                    .setDuration(200)
                    .start();
            isResultVisible = true;
        }
    }

    /**
     * Hides the active result view and restores the friendly placeholder.
     */
    private void clearResult() {
        if (isResultVisible) {
            binding.layoutResultContent.setVisibility(View.GONE);
            binding.layoutPlaceholder.setVisibility(View.VISIBLE);
            isResultVisible = false;
        }
        currentEquationText = "";
        currentResultLargeText = "";
        currentFormulaText = "";
    }

    /**
     * Copies the result to the system clipboard and displays a Snackbar confirmation.
     */
    private void copyResultToClipboard() {
        if (!isResultVisible || currentEquationText.isEmpty()) return;

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            ClipData clip = ClipData.newPlainText("UnitSwitch Result", currentEquationText);
            clipboard.setPrimaryClip(clip);
            Snackbar.make(binding.getRoot(), R.string.snackbar_copied, Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * Generates a readable formula description for temperature conversions.
     */
    @NonNull
    private String getTemperatureFormulaDescription(@NonNull String fromId, @NonNull String toId) {
        if (fromId.equalsIgnoreCase("C") && toId.equalsIgnoreCase("F")) return "Formula: (°C × 9/5) + 32 = °F";
        if (fromId.equalsIgnoreCase("F") && toId.equalsIgnoreCase("C")) return "Formula: (°F − 32) × 5/9 = °C";
        if (fromId.equalsIgnoreCase("C") && toId.equalsIgnoreCase("K")) return "Formula: °C + 273.15 = K";
        if (fromId.equalsIgnoreCase("K") && toId.equalsIgnoreCase("C")) return "Formula: K − 273.15 = °C";
        if (fromId.equalsIgnoreCase("F") && toId.equalsIgnoreCase("K")) return "Formula: (°F − 32) × 5/9 + 273.15 = K";
        if (fromId.equalsIgnoreCase("K") && toId.equalsIgnoreCase("F")) return "Formula: (K − 273.15) × 9/5 + 32 = °F";
        return "";
    }

    /**
     * Hides the virtual keyboard.
     */
    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    /**
     * Persists the user's last selected category in SharedPreferences.
     */
    private void saveLastUsedCategory(@NonNull Category category) {
        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(Constants.KEY_LAST_CATEGORY, category.name()).apply();
    }

    /**
     * Restores the user's last selected category from SharedPreferences.
     */
    private void restoreLastUsedCategory() {
        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        String categoryName = prefs.getString(Constants.KEY_LAST_CATEGORY, Category.LENGTH.name());
        try {
            currentCategory = Category.valueOf(categoryName);
        } catch (IllegalArgumentException e) {
            currentCategory = Category.LENGTH;
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Constants.KEY_STATE_CATEGORY, currentCategory.name());
        outState.putInt(Constants.KEY_STATE_SOURCE_UNIT, binding.spinnerSourceUnit.getSelectedItemPosition());
        outState.putInt(Constants.KEY_STATE_TARGET_UNIT, binding.spinnerTargetUnit.getSelectedItemPosition());

        String inputVal = binding.editTextValue.getText() != null ? binding.editTextValue.getText().toString() : "";
        outState.putString(Constants.KEY_STATE_INPUT_TEXT, inputVal);

        outState.putBoolean(Constants.KEY_STATE_RESULT_SHOWN, isResultVisible);
        outState.putString(Constants.KEY_STATE_RESULT_LARGE, currentResultLargeText);
        outState.putString(Constants.KEY_STATE_RESULT_EQUATION, currentEquationText);
        outState.putString(Constants.KEY_STATE_RESULT_FORMULA, currentFormulaText);
    }

    /**
     * Faithfully restores complete Activity state after configuration change / screen rotation.
     */
    private void restoreInstanceState(@NonNull Bundle savedInstanceState) {
        String catName = savedInstanceState.getString(Constants.KEY_STATE_CATEGORY, Category.LENGTH.name());
        try {
            currentCategory = Category.valueOf(catName);
        } catch (IllegalArgumentException e) {
            currentCategory = Category.LENGTH;
        }

        int sourcePos = savedInstanceState.getInt(Constants.KEY_STATE_SOURCE_UNIT, 0);
        int targetPos = savedInstanceState.getInt(Constants.KEY_STATE_TARGET_UNIT, 1);

        // Check appropriate chip
        int chipId = getChipIdForCategory(currentCategory);
        Chip chip = findViewById(chipId);
        if (chip != null) {
            chip.setChecked(true);
        }

        // Setup Spinners without triggering reset
        selectCategory(currentCategory, sourcePos, targetPos, false);

        // Restore input text
        String savedInput = savedInstanceState.getString(Constants.KEY_STATE_INPUT_TEXT, "");
        binding.editTextValue.setText(savedInput);

        // Restore result if it was visible
        boolean wasResultShown = savedInstanceState.getBoolean(Constants.KEY_STATE_RESULT_SHOWN, false);
        if (wasResultShown) {
            currentResultLargeText = savedInstanceState.getString(Constants.KEY_STATE_RESULT_LARGE, "");
            currentEquationText = savedInstanceState.getString(Constants.KEY_STATE_RESULT_EQUATION, "");
            currentFormulaText = savedInstanceState.getString(Constants.KEY_STATE_RESULT_FORMULA, "");
            displayResult(currentResultLargeText, currentEquationText, currentFormulaText);
        }
    }
}
