package com.predistock.util;

import java.util.List;

/**
 * Pure manual mathematical utility class for statistical forecasting.
 * STRICTLY NO machine learning or third-party statistical libraries used (e.g. Weka, Commons Math).
 * Demonstrates:
 * - Utility Class Pattern (Private constructor, pure static functions)
 * - Manual arithmetic implementations for Mean, Sum, and Sum of Products
 */
public final class MathUtil {

    private MathUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Calculates the sum of a list of double values.
     *
     * @param v list of numbers
     * @return sum (\Sigma v)
     */
    public static double sum(List<Double> v) {
        if (v == null || v.isEmpty()) {
            return 0.0;
        }
        double total = 0.0;
        for (Double val : v) {
            if (val != null) {
                total += val;
            }
        }
        return total;
    }

    /**
     * Calculates the arithmetic mean (average) of a list of double values.
     *
     * @param v list of numbers
     * @return mean (\bar{v})
     */
    public static double mean(List<Double> v) {
        if (v == null || v.isEmpty()) {
            return 0.0;
        }
        return sum(v) / v.size();
    }

    /**
     * Calculates the sum of products between two equal-length lists (\Sigma (a_i * b_i)).
     *
     * @param a first list of values
     * @param b second list of values
     * @return sum of cross-products
     */
    public static double sumProduct(List<Double> a, List<Double> b) {
        if (a == null || b == null || a.size() != b.size() || a.isEmpty()) {
            throw new IllegalArgumentException("List input harus berukuran sama dan tidak boleh kosong");
        }
        double total = 0.0;
        for (int i = 0; i < a.size(); i++) {
            total += a.get(i) * b.get(i);
        }
        return total;
    }

    /**
     * Calculates the sum of squares of a list (\Sigma (v_i^2)).
     *
     * @param v list of numbers
     * @return sum of squares
     */
    public static double sumSquare(List<Double> v) {
        if (v == null || v.isEmpty()) {
            return 0.0;
        }
        double total = 0.0;
        for (Double val : v) {
            total += (val * val);
        }
        return total;
    }

    /**
     * Helper to round a double to specified decimal places for clean UI reporting.
     *
     * @param value  double value
     * @param places decimal precision
     * @return rounded value
     */
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
}

