package com.mudassir.railbook.service;

/**
 * Exception thrown for generic authentication failures without leaking details.
 *
 * @author Mudassir Ahmed
 */
public class AuthenticationException extends Exception {

    public AuthenticationException(String message) {
        super(message);
    }
}
