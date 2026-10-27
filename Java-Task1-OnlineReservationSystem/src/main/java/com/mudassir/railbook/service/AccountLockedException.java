package com.mudassir.railbook.service;

/**
 * Exception thrown when a user or form is locked due to excessive failed login attempts.
 *
 * @author Mudassir Ahmed
 */
public class AccountLockedException extends AuthenticationException {

    private final long remainingSeconds;

    public AccountLockedException(long remainingSeconds) {
        super("Too many failed attempts. Account locked for " + remainingSeconds + " seconds.");
        this.remainingSeconds = remainingSeconds;
    }

    public long getRemainingSeconds() {
        return remainingSeconds;
    }
}
