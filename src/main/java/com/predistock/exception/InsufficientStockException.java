package com.predistock.exception;

/**
 * Thrown when an operation tries to reduce stock beyond available inventory.
 * Inherits from AppException.
 */
public class InsufficientStockException extends AppException {

    public InsufficientStockException(String message) {
        super(message);
    }
}

