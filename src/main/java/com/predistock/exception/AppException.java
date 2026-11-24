package com.predistock.exception;

/**
 * Base custom exception for PrediStock.
 * Demonstrates inheritance hierarchy in exception handling.
 */
public class AppException extends RuntimeException {
    
    public AppException(String message) {
        super(message);
    }

    public AppException(String message, Throwable cause) {
        super(message, cause);
    }
}

