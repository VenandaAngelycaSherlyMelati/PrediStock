package com.predistock.model;

import java.time.LocalDate;

/**
 * Concrete class representing perishable food and beverage products.
 * Demonstrates:
 * - Inheritance (extends Product)
 * - Polymorphism (Overrides getStorageInfo(), getCategory())
 * - Date comparison business logic for perishables
 */
public class FoodProduct extends Product {
    private LocalDate expiredDate;

    public FoodProduct(int id, String name, double price, int stock, int minStock, LocalDate expiredDate) {
        super(id, name, price, stock, minStock);
        if (expiredDate == null) {
            throw new IllegalArgumentException("Tanggal kedaluwarsa produk makanan tidak boleh null");
        }
        this.expiredDate = expiredDate;
    }

    public FoodProduct(String name, double price, int stock, int minStock, LocalDate expiredDate) {
        this(0, name, price, stock, minStock, expiredDate);
    }

    public LocalDate getExpiredDate() {
        return expiredDate;
    }

    public void setExpiredDate(LocalDate expiredDate) {
        if (expiredDate == null) {
            throw new IllegalArgumentException("Tanggal kedaluwarsa tidak boleh null");
        }
        this.expiredDate = expiredDate;
    }

    /**
     * Checks if product has already passed its expiration date.
     *
     * @return true if current date is after expiredDate
     */
    public boolean isExpired() {
        return LocalDate.now().isAfter(expiredDate);
    }

    /**
     * Checks if product will expire within the given number of days.
     *
     * @param days warning window in days
     * @return true if expiring soon or already expired
     */
    public boolean isNearExpired(int days) {
        if (days < 0) {
            days = 0;
        }
        LocalDate warningThreshold = LocalDate.now().plusDays(days);
        return !isExpired() && (expiredDate.isBefore(warningThreshold) || expiredDate.isEqual(warningThreshold));
    }

    @Override
    public String getStorageInfo() {
        return "Simpan pada tempat kering dan sejuk, hindari sinar matahari langsung. Kedaluwarsa: " + expiredDate;
    }

    @Override
    public String getCategory() {
        return "FOOD";
    }

    @Override
    public String toString() {
        return "FoodProduct{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", stock=" + stock +
                ", minStock=" + minStock +
                ", expiredDate=" + expiredDate +
                ", isExpired=" + isExpired() +
                '}';
    }
}

