package com.mudassir.railbook.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates validation results and associated friendly error messages.
 *
 * @author Mudassir Ahmed
 */
public class ValidationResult {

    private final List<String> errors = new ArrayList<>();

    public void addError(String message) {
        if (message != null && !message.isBlank()) {
            errors.add(message);
        }
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public String getFirstErrorMessage() {
        return errors.isEmpty() ? "" : errors.get(0);
    }

    public String getFormattedErrorMessage() {
        return String.join("\n• ", errors);
    }
}
