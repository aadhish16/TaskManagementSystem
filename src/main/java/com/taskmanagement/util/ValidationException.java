package com.taskmanagement.util;

/**
 * Custom checked exception thrown when user input is invalid.
 * Demonstrates INHERITANCE (extends Exception).
 */
public class ValidationException extends Exception {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
