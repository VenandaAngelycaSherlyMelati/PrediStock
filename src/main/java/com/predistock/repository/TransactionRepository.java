package com.predistock.repository;

import com.predistock.discount.Discount;
import com.predistock.discount.NominalDiscount;
import com.predistock.discount.PercentDiscount;
import com.predistock.model.Cashier;
import com.predistock.model.DailySales;
import com.predistock.model.FoodProduct;
import com.predistock.model.GoodsProduct;
import com.predistock.model.Product;
import com.predistock.model.Transaction;
import com.predistock.model.TransactionItem;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository implementation for Sales Transactions using raw JDBC.
 * Demonstrates:
 * - Implementation of Generic Interface (Repository<Transaction>)
 * - ACID Transaction Management (setAutoCommit(false), commit(), rollback())
 * - Composition & Relationship Mapping between Transactions and Line Items
 * - Raw JDBC operations with PreparedStatement and ResultSet (No JPA/Hibernate)
 */
@Repository
public class TransactionRepository implements com.predistock.repository.Repository<Transaction> {

    private static final DateTimeFormatter ISO_DTF = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public void save(Transaction trx) {
        String insertHeaderSql = """
                INSERT INTO transactions (date, cashier_id, discount_type, discount_value, total)
                VALUES (?, ?, ?, ?, ?)
                """;

        String insertItemSql = """
                INSERT INTO transaction_items (transaction_id, product_id, qty, unit_price)
                VALUES (?, ?, ?, ?)
                """;

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Begin JDBC transaction for atomicity

            // 1. Insert Transaction Header
            int generatedId;
            try (PreparedStatement pstmt = conn.prepareStatement(insertHeaderSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, trx.getDate().format(ISO_DTF));
                pstmt.setInt(2, trx.getCashier() != null ? trx.getCashier().getId() : 1);

                Discount d = trx.getDiscount();
                if (d instanceof PercentDiscount pd) {
                    pstmt.setString(3, "PERCENT");
                    pstmt.setDouble(4, pd.getPercent());
                } else if (d instanceof NominalDiscount nd) {
                    pstmt.setString(3, "NOMINAL");
                    pstmt.setDouble(4, nd.getAmount());
                } else {
                    pstmt.setNull(3, Types.VARCHAR);
                    pstmt.setNull(4, Types.REAL);
                }

                pstmt.setDouble(5, trx.calculateTotal());
                pstmt.executeUpdate();

                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        generatedId = keys.getInt(1);
                        trx.setId(generatedId);
                    } else {
                        throw new SQLException("Failed to obtain generated ID for transaction");
                    }
                }
            }

            // 2. Insert Transaction Items (Composition)
            try (PreparedStatement itemStmt = conn.prepareStatement(insertItemSql)) {
                for (TransactionItem item : trx.getItems()) {
                    itemStmt.setInt(1, trx.getId());
                    itemStmt.setInt(2, item.getProduct().getId());
                    itemStmt.setInt(3, item.getQty());
                    itemStmt.setDouble(4, item.getUnitPrice());
                    itemStmt.addBatch();
                }
                itemStmt.executeBatch();
            }

            conn.commit(); // Commit transaction
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Rollback failed: " + ex.getMessage());
                }
            }
            throw new RuntimeException("Error saving transaction to database: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Failed to close connection: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public Transaction findById(int id) {
        String headerSql = """
                SELECT t.id, t.date, t.cashier_id, t.discount_type, t.discount_value, t.total,
                       u.username, u.name as cashier_name, u.role, u.shift_number
                FROM transactions t
                LEFT JOIN users u ON t.cashier_id = u.id
                WHERE t.id = ?
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(headerSql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Transaction trx = mapRowToTransaction(rs);
                    trx.getItems().addAll(loadItemsForTransaction(conn, trx.getId()));
                    return trx;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding transaction by id: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<Transaction> findAll() {
        List<Transaction> list = new ArrayList<>();
        String headerSql = """
                SELECT t.id, t.date, t.cashier_id, t.discount_type, t.discount_value, t.total,
                       u.username, u.name as cashier_name, u.role, u.shift_number
                FROM transactions t
                LEFT JOIN users u ON t.cashier_id = u.id
                ORDER BY t.date DESC
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(headerSql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Transaction trx = mapRowToTransaction(rs);
                trx.getItems().addAll(loadItemsForTransaction(conn, trx.getId()));
                list.add(trx);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all transactions: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public void update(Transaction entity) {
        String sql = "UPDATE transactions SET discount_type = ?, discount_value = ?, total = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            Discount d = entity.getDiscount();
            if (d instanceof PercentDiscount pd) {
                pstmt.setString(1, "PERCENT");
                pstmt.setDouble(2, pd.getPercent());
            } else if (d instanceof NominalDiscount nd) {
                pstmt.setString(1, "NOMINAL");
                pstmt.setDouble(2, nd.getAmount());
            } else {
                pstmt.setNull(1, Types.VARCHAR);
                pstmt.setNull(2, Types.REAL);
            }
            pstmt.setDouble(3, entity.calculateTotal());
            pstmt.setInt(4, entity.getId());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating transaction: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) {
        String deleteItemsSql = "DELETE FROM transaction_items WHERE transaction_id = ?";
        String deleteHeaderSql = "DELETE FROM transactions WHERE id = ?";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(deleteItemsSql)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
            }

            try (PreparedStatement pstmt = conn.prepareStatement(deleteHeaderSql)) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw new RuntimeException("Error deleting transaction: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Finds transactions within a given date range (inclusive).
     * Uses SUBSTR(t.date, 1, 10) to reliably match both ISO format (2026-11-11T10:00:00)
     * and SQL datetime format (2026-11-11 10:00:00).
     */
    public List<Transaction> findByDateRange(LocalDate from, LocalDate to) {
        List<Transaction> list = new ArrayList<>();
        String fromStr = from.toString();
        String toStr = to.toString();

        String sql = """
                SELECT t.id, t.date, t.cashier_id, t.discount_type, t.discount_value, t.total,
                       u.username, u.name as cashier_name, u.role, u.shift_number
                FROM transactions t
                LEFT JOIN users u ON t.cashier_id = u.id
                WHERE SUBSTR(t.date, 1, 10) >= ? AND SUBSTR(t.date, 1, 10) <= ?
                ORDER BY t.date ASC
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, fromStr);
            pstmt.setString(2, toStr);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Transaction trx = mapRowToTransaction(rs);
                    trx.getItems().addAll(loadItemsForTransaction(conn, trx.getId()));
                    list.add(trx);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error querying transactions by date range: " + e.getMessage(), e);
        }
        return list;
    }

    /**
     * Aggregates daily sales quantity for a specific product.
     * Core integration point for Module 4 (Forecasting).
     *
     * @param productId target product ID
     * @return chronological list of DailySales
     */
    public List<DailySales> findDailySales(int productId) {
        List<DailySales> sales = new ArrayList<>();
        String sql = """
                SELECT SUBSTR(t.date, 1, 10) as sale_date, ti.product_id, SUM(ti.qty) as total_qty
                FROM transaction_items ti
                JOIN transactions t ON ti.transaction_id = t.id
                WHERE ti.product_id = ?
                GROUP BY SUBSTR(t.date, 1, 10), ti.product_id
                ORDER BY sale_date ASC
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    LocalDate date = LocalDate.parse(rs.getString("sale_date"));
                    int pId = rs.getInt("product_id");
                    int qty = rs.getInt("total_qty");
                    sales.add(new DailySales(date, pId, qty));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error querying daily sales for product " + productId + ": " + e.getMessage(), e);
        }
        return sales;
    }

    private List<TransactionItem> loadItemsForTransaction(Connection conn, int trxId) throws SQLException {
        List<TransactionItem> items = new ArrayList<>();
        String sql = """
                SELECT ti.id, ti.qty, ti.unit_price,
                       p.id as prod_id, p.name, p.price, p.stock, p.min_stock, p.type, p.expired_date, p.brand
                FROM transaction_items ti
                JOIN products p ON ti.product_id = p.id
                WHERE ti.transaction_id = ?
                """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, trxId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int pId = rs.getInt("prod_id");
                    String name = rs.getString("name");
                    double price = rs.getDouble("price");
                    int stock = rs.getInt("stock");
                    int minStock = rs.getInt("min_stock");
                    String type = rs.getString("type");
                    String expiredStr = rs.getString("expired_date");
                    String brand = rs.getString("brand");

                    Product product;
                    if ("FOOD".equalsIgnoreCase(type)) {
                        LocalDate expDate = (expiredStr != null && !expiredStr.isEmpty())
                                ? LocalDate.parse(expiredStr)
                                : LocalDate.now().plusMonths(1);
                        product = new FoodProduct(pId, name, price, stock, minStock, expDate);
                    } else {
                        product = new GoodsProduct(pId, name, price, stock, minStock, brand);
                    }

                    int qty = rs.getInt("qty");
                    double unitPrice = rs.getDouble("unit_price");
                    items.add(new TransactionItem(product, qty, unitPrice));
                }
            }
        }
        return items;
    }

    private Transaction mapRowToTransaction(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        LocalDateTime date = parseDateTimeDefensive(rs.getString("date"));

        int cashierId = rs.getInt("cashier_id");
        String cashierName = rs.getString("cashier_name");
        String username = rs.getString("username");
        int shift = rs.getInt("shift_number");

        Cashier cashier = new Cashier(cashierId, username != null ? username : "kasir", "", cashierName != null ? cashierName : "Kasir", shift);

        Transaction trx = new Transaction(id, cashier, date);

        String discountType = rs.getString("discount_type");
        double discountValue = rs.getDouble("discount_value");
        if ("PERCENT".equalsIgnoreCase(discountType)) {
            trx.setDiscount(new PercentDiscount(discountValue));
        } else if ("NOMINAL".equalsIgnoreCase(discountType)) {
            trx.setDiscount(new NominalDiscount(discountValue));
        }

        return trx;
    }

    private LocalDateTime parseDateTimeDefensive(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return LocalDateTime.now();
        }
        String clean = dateStr.trim();
        if (clean.contains(" ") && !clean.contains("T")) {
            clean = clean.replace(" ", "T");
        }
        try {
            return LocalDateTime.parse(clean, ISO_DTF);
        } catch (Exception e1) {
            try {
                if (clean.length() >= 10) {
                    return LocalDate.parse(clean.substring(0, 10)).atStartOfDay();
                }
            } catch (Exception ignored) {}
            return LocalDateTime.now();
        }
    }
}

