package com.predistock.model;

import com.predistock.discount.Discount;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Concrete class representing a Point of Sale Sales Transaction.
 * Demonstrates:
 * - Composition (Transaction *-- TransactionItem): Line items are created and managed by the transaction.
 * - Association (Transaction -> Cashier).
 * - Polymorphism: Uses Discount interface reference (PercentDiscount / NominalDiscount).
 * - Encapsulation of financial subtotal, total, and change calculations.
 */
public class Transaction {
    private int id;
    private LocalDateTime date;
    private Cashier cashier;
    private final List<TransactionItem> items;
    private Discount discount;

    public Transaction(int id, Cashier cashier, LocalDateTime date) {
        this.id = id;
        this.cashier = cashier;
        this.date = date != null ? date : LocalDateTime.now();
        this.items = new ArrayList<>();
    }

    public Transaction(int id, Cashier cashier) {
        this(id, cashier, LocalDateTime.now());
    }

    public Transaction(Cashier cashier) {
        this(0, cashier, LocalDateTime.now());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Cashier getCashier() {
        return cashier;
    }

    public void setCashier(Cashier cashier) {
        this.cashier = cashier;
    }

    public List<TransactionItem> getItems() {
        return items;
    }

    public Discount getDiscount() {
        return discount;
    }

    public void setDiscount(Discount discount) {
        this.discount = discount;
    }

    /**
     * Demonstrates Composition: creates and embeds a new TransactionItem inside this Transaction.
     *
     * @param product product to purchase
     * @param qty     quantity purchased
     */
    public void addItem(Product product, int qty) {
        if (product == null) {
            throw new IllegalArgumentException("Produk tidak boleh null");
        }
        if (qty <= 0) {
            throw new IllegalArgumentException("Jumlah item harus lebih dari 0");
        }
        this.items.add(new TransactionItem(product, qty, product.getPrice()));
    }

    /**
     * Adds an already constructed TransactionItem line.
     *
     * @param item TransactionItem
     */
    public void addItem(TransactionItem item) {
        if (item != null) {
            this.items.add(item);
        }
    }

    /**
     * Calculates the raw subtotal sum of all item lines before discount.
     *
     * @return subtotal amount
     */
    public double calculateSubtotal() {
        double subtotal = 0.0;
        for (TransactionItem item : items) {
            subtotal += item.getSubtotal();
        }
        return subtotal;
    }

    /**
     * Calculates the final total amount payable after applying polymorphism discount logic.
     *
     * @return total payable
     */
    public double calculateTotal() {
        double subtotal = calculateSubtotal();
        if (discount != null) {
            // Polymorphic dispatch: invokes PercentDiscount.apply() or NominalDiscount.apply()
            return discount.apply(subtotal);
        }
        return subtotal;
    }

    /**
     * Calculates customer change given amount paid.
     *
     * @param paid money paid by customer
     * @return change amount
     * @throws IllegalArgumentException if paid amount is insufficient
     */
    public double calculateChange(double paid) {
        double total = calculateTotal();
        if (paid < total) {
            throw new IllegalArgumentException(String.format("Uang yang dibayarkan (Rp %,.0f) kurang dari total tagihan (Rp %,.0f)", paid, total));
        }
        return paid - total;
    }

    /**
     * Generates a plain-text printable transaction receipt (Fitur C-04).
     *
     * @return formatted receipt string
     */
    public String generateReceipt() {
        return generateReceipt(calculateTotal()); // default exact paid
    }

    /**
     * Generates a plain-text receipt with paid amount and change.
     *
     * @param paid cash received
     * @return formatted receipt string
     */
    public String generateReceipt(double paid) {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

        Store store = com.predistock.repository.DatabaseConnection.getStoreProfile();
        String storeName = (store != null && store.getName() != null && !store.getName().trim().isEmpty())
                ? store.getName().toUpperCase()
                : "PREDISTOCK POS";
        String storeAddr = (store != null && store.getAddress() != null && !store.getAddress().trim().isEmpty())
                ? store.getAddress()
                : "Manajemen Kasir & Stok UMKM";

        sb.append("========================================\n");
        sb.append(centerText(storeName, 40)).append("\n");
        sb.append(centerText(storeAddr, 40)).append("\n");
        sb.append("========================================\n");
        sb.append(String.format("No. Transaksi : #TRX-%05d\n", id));
        sb.append("Tanggal       : ").append(date.format(dtf)).append("\n");
        String cashierLine = "Umum";
        if (cashier != null) {
            cashierLine = cashier.getShiftNumber() > 0 
                ? cashier.getName() + " (Shift " + cashier.getShiftNumber() + ")" 
                : cashier.getName();
        }
        sb.append(String.format("Kasir         : %s\n", cashierLine));
        sb.append("----------------------------------------\n");
        sb.append(String.format("%-18s %4s %8s %8s\n", "Item", "Qty", "Harga", "Subtotal"));
        sb.append("----------------------------------------\n");

        for (TransactionItem item : items) {
            String prodName = item.getProduct().getName();
            if (prodName.length() > 17) {
                prodName = prodName.substring(0, 14) + "...";
            }
            sb.append(String.format("%-18s %4d %,8.0f %,8.0f\n",
                    prodName,
                    item.getQty(),
                    item.getUnitPrice(),
                    item.getSubtotal()));
        }

        double subtotal = calculateSubtotal();
        double total = calculateTotal();
        double discountAmount = subtotal - total;
        double change = paid >= total ? (paid - total) : 0.0;

        sb.append("----------------------------------------\n");
        sb.append(String.format("Subtotal                : Rp %,10.0f\n", subtotal));
        if (discount != null) {
            sb.append(String.format("Diskon (%-15s): -Rp %,9.0f\n", discount.getDescription(), discountAmount));
        }
        sb.append(String.format("TOTAL                   : Rp %,10.0f\n", total));
        sb.append(String.format("Tunai Dibayar           : Rp %,10.0f\n", paid));
        sb.append(String.format("Kembalian               : Rp %,10.0f\n", change));
        sb.append("========================================\n");
        sb.append("     Terima Kasih Atas Kunjungan Anda   \n");
        sb.append("========================================\n");

        return sb.toString();
    }

    private String centerText(String text, int width) {
        if (text == null) text = "";
        if (text.length() >= width) return text.substring(0, width);
        int pad = (width - text.length()) / 2;
        return " ".repeat(pad) + text;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", date=" + date +
                ", cashier=" + (cashier != null ? cashier.getName() : "null") +
                ", itemsCount=" + items.size() +
                ", total=" + calculateTotal() +
                '}';
    }
}

