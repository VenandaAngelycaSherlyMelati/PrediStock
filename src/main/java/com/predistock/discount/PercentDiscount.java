package com.predistock.discount;

/**
 * Concrete implementation of Discount for percentage-based cuts (e.g. 10%).
 * Demonstrates:
 * - Interface Implementation
 * - Polymorphism (apply calculates percentage deduction)
 */
public class PercentDiscount implements Discount {
    private final double percent;

    public PercentDiscount(double percent) {
        if (percent < 0 || percent > 100) {
            throw new IllegalArgumentException("Persentase diskon harus berada di antara 0 dan 100%");
        }
        this.percent = percent;
    }

    public double getPercent() {
        return percent;
    }

    @Override
    public double apply(double subtotal) {
        if (subtotal <= 0) {
            return 0.0;
        }
        double discountAmount = subtotal * (percent / 100.0);
        return Math.max(0.0, subtotal - discountAmount);
    }

    @Override
    public String getDescription() {
        return "Diskon " + percent + "%";
    }

    @Override
    public String toString() {
        return getDescription();
    }
}

