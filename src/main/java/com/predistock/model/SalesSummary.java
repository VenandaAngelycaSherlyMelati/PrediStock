package com.predistock.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Model class encapsulating aggregated sales performance metrics for a specific time window.
 * Demonstrates:
 * - Encapsulation of financial business metrics
 * - Collection Manipulation (Sorting Map.Entry by values using Collections & Stream)
 */
public class SalesSummary {
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private double totalRevenue;
    private int totalTransactions;
    private Map<String, Integer> qtyPerProduct;

    public SalesSummary(LocalDate periodStart, LocalDate periodEnd, double totalRevenue, int totalTransactions, Map<String, Integer> qtyPerProduct) {
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.totalRevenue = Math.max(0.0, totalRevenue);
        this.totalTransactions = Math.max(0, totalTransactions);
        this.qtyPerProduct = (qtyPerProduct != null) ? new HashMap<>(qtyPerProduct) : new HashMap<>();
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(LocalDate periodStart) {
        this.periodStart = periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(LocalDate periodEnd) {
        this.periodEnd = periodEnd;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = Math.max(0.0, totalRevenue);
    }

    public int getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(int totalTransactions) {
        this.totalTransactions = Math.max(0, totalTransactions);
    }

    public Map<String, Integer> getQtyPerProduct() {
        return qtyPerProduct;
    }

    public void setQtyPerProduct(Map<String, Integer> qtyPerProduct) {
        this.qtyPerProduct = (qtyPerProduct != null) ? new HashMap<>(qtyPerProduct) : new HashMap<>();
    }

    /**
     * Feature E-03: Ranks the top N best-selling products by quantity sold.
     * Demonstrates Collection manipulation by sorting Map entries in descending order.
     *
     * @param n number of top products to extract
     * @return ordered list of product names formatted with sold count
     */
    public List<String> getTopProducts(int n) {
        if (qtyPerProduct.isEmpty() || n <= 0) {
            return Collections.emptyList();
        }

        // Sort entries by quantity descending
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(qtyPerProduct.entrySet());
        entries.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        List<String> topList = new ArrayList<>();
        int limit = Math.min(n, entries.size());
        for (int i = 0; i < limit; i++) {
            Map.Entry<String, Integer> entry = entries.get(i);
            topList.add(String.format("%s (%d unit)", entry.getKey(), entry.getValue()));
        }

        return topList;
    }

    @Override
    public String toString() {
        return "SalesSummary{" +
                "period=" + periodStart + " to " + periodEnd +
                ", totalRevenue=" + totalRevenue +
                ", totalTransactions=" + totalTransactions +
                ", distinctProducts=" + qtyPerProduct.size() +
                '}';
    }
}

