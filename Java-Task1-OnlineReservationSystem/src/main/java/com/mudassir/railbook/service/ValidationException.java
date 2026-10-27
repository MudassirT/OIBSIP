package com.mudassir.railbook.service;

/**
 * Exception thrown when business or format validation rules fail.
 *
 * @author Mudassir Ahmed
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}
