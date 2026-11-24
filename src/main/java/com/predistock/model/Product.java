package com.predistock.model;

import com.predistock.exception.InsufficientStockException;

/**
 * Abstract class representing a general inventory product.
 * Demonstrates:
 * - Abstract Class & Abstract Methods
 * - Encapsulation (Stock can only be modified through business logic methods)
 * - Polymorphism Contract (getStorageInfo(), getCategory())
 */
public abstract class Product {
    protected int id;
    protected String name;
    protected double price;
    protected int stock;
    protected int minStock;

    public Product(int id, String name, double price, int stock, int minStock) {
        if (price < 0) {
            throw new IllegalArgumentException("Harga tidak boleh negatif");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("Stok awal tidak boleh negatif");
        }
        if (minStock < 0) {
            throw new IllegalArgumentException("Batas stok minimum tidak boleh negatif");
        }
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.minStock = minStock;
    }

    public Product(String name, double price, int stock, int minStock) {
        this(0, name, price, stock, minStock);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        if (price < 0) {
            throw new IllegalArgumentException("Harga tidak boleh negatif");
        }
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public int getMinStock() {
        return minStock;
    }

    public void setMinStock(int minStock) {
        if (minStock < 0) {
            throw new IllegalArgumentException("Batas stok minimum tidak boleh negatif");
        }
        this.minStock = minStock;
    }

    /**
     * Business method to add stock inventory (Restock).
     *
     * @param qty amount of goods to add
     */
    public void addStock(int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("Jumlah penambahan stok harus lebih besar dari 0");
        }
        this.stock += qty;
    }

    /**
     * Business method to safely reduce stock inventory.
     * Prevents negative inventory by strictly validating current stock.
     *
     * @param qty amount of goods to deduct
     * @throws InsufficientStockException if requested qty exceeds available stock
     */
    public void reduceStock(int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("Jumlah pengurangan stok harus lebih besar dari 0");
        }
        if (this.stock < qty) {
            throw new InsufficientStockException("Stok produk '" + name + "' tidak mencukupi (Tersedia: " + this.stock + ", Diminta: " + qty + ")");
        }
        this.stock -= qty;
    }

    /**
     * Checks if product stock has dropped below or equal to the minimum threshold.
     *
     * @return true if stock <= minStock
     */
    public boolean isLowStock() {
        return this.stock <= this.minStock;
    }

    /**
     * Abstract method to obtain specific storage and handling instructions.
     */
    public abstract String getStorageInfo();

    /**
     * Abstract method to obtain category classification (e.g., "FOOD", "GOODS").
     */
    public abstract String getCategory();

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", stock=" + stock +
                ", minStock=" + minStock +
                ", category='" + getCategory() + '\'' +
                '}';
    }
}

