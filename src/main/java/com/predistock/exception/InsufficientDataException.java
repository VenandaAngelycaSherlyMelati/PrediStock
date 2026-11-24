package com.predistock.exception;

/**
 * Thrown when historical sales data is insufficient to compute forecasting or accuracy.
 * Inherits from AppException.
 */
public class InsufficientDataException extends AppException {

    public InsufficientDataException(String message) {
        super(message);
    }
}

