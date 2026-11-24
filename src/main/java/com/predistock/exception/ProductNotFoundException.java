package com.predistock.exception;

/**
 * Thrown when a requested product ID is not found in the database.
 * Inherits from AppException.
 */
public class ProductNotFoundException extends AppException {

    public ProductNotFoundException(String message) {
        super(message);
    }
}

