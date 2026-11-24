package com.predistock.discount;

/**
 * Interface defining the contract for calculating transaction discounts.
 * Demonstrates:
 * - Interface
 * - Polymorphism Contract (Enables dynamic discount calculation strategies)
 */
public interface Discount {

    /**
     * Applies discount calculation to the given subtotal.
     * Note: Returns the new subtotal after discount is deducted.
     *
     * @param subtotal original subtotal amount
     * @return final payable amount after applying discount
     */
    double apply(double subtotal);

    /**
     * Human-readable description of the discount policy.
     *
     * @return description string
     */
    String getDescription();
}

