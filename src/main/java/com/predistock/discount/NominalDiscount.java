package com.predistock.discount;

/**
 * Concrete implementation of Discount for fixed rupiah cuts (e.g. Rp 5.000).
 * Demonstrates:
 * - Interface Implementation
 * - Polymorphism (apply calculates fixed nominal deduction)
 */
public class NominalDiscount implements Discount {
    private final double amount;

    public NominalDiscount(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Nominal diskon tidak boleh negatif");
        }
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }

    @Override
    public double apply(double subtotal) {
        if (subtotal <= 0) {
            return 0.0;
        }
        return Math.max(0.0, subtotal - amount);
    }

    @Override
    public String getDescription() {
        return String.format("Diskon Nominal Rp %,.0f", amount);
    }

    @Override
    public String toString() {
        return getDescription();
    }
}

