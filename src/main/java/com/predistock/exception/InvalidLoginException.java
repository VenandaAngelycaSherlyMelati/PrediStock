package com.predistock.exception;

/**
 * Thrown when user credentials do not match or user is not found.
 * Inherits from AppException.
 */
public class InvalidLoginException extends AppException {

    public InvalidLoginException(String message) {
        super(message);
    }
}

