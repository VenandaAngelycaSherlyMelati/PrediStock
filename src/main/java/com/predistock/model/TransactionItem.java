package com.predistock.model;

/**
 * Line item within a sales transaction.
 * Demonstrates:
 * - Composition Part: Exists exclusively as an integral item within a Transaction.
 * - Association (TransactionItem -> Product).
 * - Encapsulation (Encapsulates unit price snapshot and subtotal calculation).
 */
public class TransactionItem {
    private Product product;
    private int qty;
    private double unitPrice;

    public TransactionItem(Product product, int qty, double unitPrice) {
        if (product == null) {
            throw new IllegalArgumentException("Produk pada baris transaksi tidak boleh null");
        }
        if (qty <= 0) {
            throw new IllegalArgumentException("Jumlah item (qty) harus lebih besar dari 0");
        }
        if (unitPrice < 0) {
            throw new IllegalArgumentException("Harga satuan tidak boleh negatif");
        }
        this.product = product;
        this.qty = qty;
        this.unitPrice = unitPrice;
    }

    public TransactionItem(Product product, int qty) {
        this(product, qty, product != null ? product.getPrice() : 0.0);
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("Jumlah item (qty) harus lebih besar dari 0");
        }
        this.qty = qty;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        if (unitPrice < 0) {
            throw new IllegalArgumentException("Harga satuan tidak boleh negatif");
        }
        this.unitPrice = unitPrice;
    }

    /**
     * Calculates the subtotal for this item line.
     *
     * @return qty multiplied by unitPrice
     */
    public double getSubtotal() {
        return this.qty * this.unitPrice;
    }

    @Override
    public String toString() {
        return "TransactionItem{" +
                "product=" + (product != null ? product.getName() : "null") +
                ", qty=" + qty +
                ", unitPrice=" + unitPrice +
                ", subtotal=" + getSubtotal() +
                '}';
    }
}

