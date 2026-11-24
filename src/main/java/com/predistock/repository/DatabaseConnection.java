package com.predistock.repository;

import com.predistock.model.Store;
import com.predistock.util.PasswordUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages raw JDBC connection to SQLite database and table initialization.
 * Demonstrates:
 * - Static Connection Factory
 * - Raw JDBC Execution (No JPA/Hibernate)
 * - Automated Schema Initialization & Default Seeding
 */
public class DatabaseConnection {

    private static final String DB_DIR_NAME = ".predistock";
    private static final String DB_FILE_NAME = "predistock.db";
    private static String dbUrl = null;
    private static String resolvedDbPath = null;

    // Private constructor to prevent direct instantiation
    private DatabaseConnection() {}

    /**
     * Resolves a guaranteed writable file path on Windows outside the protected Documents folder.
     * Uses ~/.predistock/predistock.db to avoid Windows Controlled Folder Access (ransomware) blocks.
     * Lets SQLite JDBC natively create and format the database file correctly.
     */
    private static synchronized String getResolvedDbUrl() {
        if (dbUrl != null) {
            return dbUrl;
        }
        if (resolvedDbPath != null) {
            return "jdbc:sqlite:" + resolvedDbPath;
        }

        try {
            java.io.File homeDir = new java.io.File(System.getProperty("user.home"), DB_DIR_NAME);
            if (!homeDir.exists()) {
                homeDir.mkdirs();
            }

            // Clean up any corrupted 4096-byte files from previous test attempts
            java.io.File targetFile = new java.io.File(homeDir, DB_FILE_NAME);
            if (targetFile.exists() && targetFile.length() <= 4096) {
                targetFile.delete();
            }

            java.io.File oldCorruptFile = new java.io.File(homeDir, "predistock_data.db");
            if (oldCorruptFile.exists() && oldCorruptFile.length() <= 4096) {
                oldCorruptFile.delete();
            }

            resolvedDbPath = targetFile.getAbsolutePath().replace('\\', '/');
            System.out.println("[DatabaseConnection] Using SQLite database at: " + resolvedDbPath);
            return "jdbc:sqlite:" + resolvedDbPath;
        } catch (Exception e) {
            System.err.println("[DatabaseConnection] Error resolving SQLite DB path: " + e.getMessage());
            return "jdbc:sqlite:" + DB_FILE_NAME;
        }
    }

    /**
     * Returns the physical file path of the active SQLite database.
     */
    public static String getDatabasePath() {
        getResolvedDbUrl();
        return resolvedDbPath != null ? resolvedDbPath : DB_FILE_NAME;
    }

    /**
     * Sets a custom database URL (useful for integration tests or custom configs).
     *
     * @param url SQLite JDBC URL
     */
    public static void setDbUrl(String url) {
        dbUrl = url;
    }

    /**
     * Obtains a new JDBC connection to the SQLite database.
     *
     * @return active java.sql.Connection
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Explicitly load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC Driver not found on classpath", e);
        }
        return DriverManager.getConnection(getResolvedDbUrl());
    }

    /**
     * Creates all database tables according to the PrediStock proposal schema.
     * Seeds initial store information and default owner/cashier accounts if empty.
     */
    public static void initTables() {
        String createUsersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password_hash TEXT NOT NULL,
                    name TEXT NOT NULL,
                    role TEXT NOT NULL,
                    shift_number INTEGER DEFAULT 1
                );
                """;

        String createStoreTable = """
                CREATE TABLE IF NOT EXISTS store (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    address TEXT NOT NULL
                );
                """;

        String createProductsTable = """
                CREATE TABLE IF NOT EXISTS products (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    price REAL NOT NULL,
                    stock INTEGER NOT NULL,
                    min_stock INTEGER NOT NULL,
                    type TEXT NOT NULL,
                    expired_date TEXT,
                    brand TEXT
                );
                """;

        String createTransactionsTable = """
                CREATE TABLE IF NOT EXISTS transactions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    date TEXT NOT NULL,
                    cashier_id INTEGER NOT NULL,
                    discount_type TEXT,
                    discount_value REAL,
                    total REAL NOT NULL,
                    FOREIGN KEY (cashier_id) REFERENCES users(id)
                );
                """;

        String createTransactionItemsTable = """
                CREATE TABLE IF NOT EXISTS transaction_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    transaction_id INTEGER NOT NULL,
                    product_id INTEGER NOT NULL,
                    qty INTEGER NOT NULL,
                    unit_price REAL NOT NULL,
                    FOREIGN KEY (transaction_id) REFERENCES transactions(id),
                    FOREIGN KEY (product_id) REFERENCES products(id)
                );
                """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createUsersTable);
            stmt.execute(createStoreTable);
            stmt.execute(createProductsTable);
            stmt.execute(createTransactionsTable);
            stmt.execute(createTransactionItemsTable);

            // Seed user's exact 14-day test dataset if empty
            seedUserTestData(conn);

        } catch (SQLException e) {
            System.err.println("[DatabaseConnection] Error initializing database tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Seeds the exact dataset specified by the user:
     * - Toko Zaini Sejahtera
     * - 3 Users: Tubagus Zaini (Owner), Ruqayyah (Kasir1), Ida (Kasir2)
     * - 4 Products: Mie Instan (linear trend), Teh Botol (fluctuating), Roti Tawar (alert), Sabun Mandi (goods)
     * - 14 Days of Transactions & Items (28 Okt - 10 Nov 2026)
     */
    public static void seedUserTestData(Connection conn) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM users";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkSql)) {
            if (rs.next() && rs.getInt(1) > 0) {
                // Ensure users table has updated valid password hashes
                ensureUserPasswordsUpToDate(conn);
                return; // Already populated
            }
        }
        forceResetAndSeed(conn);
    }

    private static void ensureUserPasswordsUpToDate(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            String ownerHash = PasswordUtil.hash("admin123");
            String cashierHash = PasswordUtil.hash("kasir123");
            stmt.executeUpdate("UPDATE users SET password_hash = '" + ownerHash + "' WHERE username = 'owner'");
            stmt.executeUpdate("UPDATE users SET password_hash = '" + cashierHash + "' WHERE username LIKE 'kasir%'");
        } catch (Exception ignored) {}
    }

    public static void forceResetAndSeed(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM transaction_items");
            stmt.execute("DELETE FROM transactions");
            stmt.execute("DELETE FROM products");
            stmt.execute("DELETE FROM users");
            stmt.execute("DELETE FROM store");

            // 1. DATA TOKO
            stmt.execute("""
                INSERT INTO store (id, name, address) 
                VALUES (1, 'Toko Zaini Sejahtera', 'Jl. Telekomunikasi No. 1, Bojongsoang, Bandung')
            """);

            // 2. DATA PENGGUNA (Hashed dynamically via PasswordUtil to guarantee match)
            String ownerHash = PasswordUtil.hash("admin123");
            String cashierHash = PasswordUtil.hash("kasir123");

            String insertUsersSql = "INSERT INTO users (id, username, password_hash, name, role, shift_number) VALUES " +
                    "(1, 'owner', '" + ownerHash + "', 'Tubagus Zaini', 'OWNER', 1), " +
                    "(2, 'kasir1', '" + cashierHash + "', 'Ruqayyah', 'CASHIER', 1), " +
                    "(3, 'kasir2', '" + cashierHash + "', 'Ida', 'CASHIER', 2)";
            stmt.execute(insertUsersSql);

            // 3. DATA PRODUK
            stmt.execute("""
                INSERT INTO products (id, name, price, stock, min_stock, type, expired_date, brand) VALUES 
                (1, 'Mie Instan Goreng', 3000, 150, 20, 'FOOD', '2027-12-31', NULL),
                (2, 'Teh Botol 350ml', 4500, 80, 15, 'FOOD', '2027-06-30', NULL),
                (3, 'Roti Tawar Kismis', 15000, 5, 10, 'FOOD', '2026-11-12', NULL),
                (4, 'Sabun Mandi Cair', 22000, 40, 10, 'GOODS', NULL, 'Lifebuoy')
            """);

            // 4. DATA TRANSAKSI (14 Hari Terakhir: 28 Okt - 10 Nov 2026)
            stmt.execute("""
                INSERT INTO transactions (id, date, cashier_id, discount_type, discount_value, total) VALUES 
                (1, '2026-10-28 10:00:00', 2, 'NONE', 0, 39000),
                (2, '2026-10-29 11:30:00', 2, 'NONE', 0, 42000),
                (3, '2026-10-30 14:15:00', 3, 'NONE', 0, 49500),
                (4, '2026-10-31 09:45:00', 2, 'NONE', 0, 52500),
                (5, '2026-11-01 16:20:00', 3, 'PERCENT', 10, 54000),
                (6, '2026-11-02 12:10:00', 2, 'NONE', 0, 54000),
                (7, '2026-11-03 13:50:00', 3, 'NONE', 0, 61500),
                (8, '2026-11-04 10:25:00', 2, 'NONE', 0, 75000),
                (9, '2026-11-05 15:40:00', 3, 'NONE', 0, 67500),
                (10, '2026-11-06 11:15:00', 2, 'NONE', 0, 66000),
                (11, '2026-11-07 14:55:00', 3, 'NONE', 0, 73500),
                (12, '2026-11-08 09:30:00', 2, 'NONE', 0, 81000),
                (13, '2026-11-09 16:05:00', 3, 'NONE', 0, 88500),
                (14, '2026-11-10 12:45:00', 2, 'NONE', 0, 82500)
            """);

            // 4b. DATA TRANSACTION ITEMS
            stmt.execute("""
                INSERT INTO transaction_items (transaction_id, product_id, qty, unit_price) VALUES 
                (1, 1, 10, 3000), (1, 2, 2, 4500),
                (2, 1, 11, 3000), (2, 2, 2, 4500),
                (3, 1, 12, 3000), (3, 2, 3, 4500),
                (4, 1, 13, 3000), (4, 2, 3, 4500),
                (5, 1, 14, 3000), (5, 2, 4, 4500),
                (6, 1, 15, 3000), (6, 2, 2, 4500),
                (7, 1, 16, 3000), (7, 2, 3, 4500),
                (8, 1, 17, 3000), (8, 4, 1, 24000),
                (9, 1, 18, 3000), (9, 2, 3, 4500),
                (10, 1, 19, 3000), (10, 2, 2, 4500),
                (11, 1, 20, 3000), (11, 2, 3, 4500),
                (12, 1, 21, 3000), (12, 2, 4, 4500),
                (13, 1, 22, 3000), (13, 2, 5, 4500),
                (14, 1, 23, 3000), (14, 2, 3, 4500)
            """);

            System.out.println("[DatabaseConnection] Successfully seeded all requested test data (Toko Zaini Sejahtera, 3 Users, 4 Products, 14 Days Transactions).");
        }
    }

    /**
     * Helper to load Store profile from SQLite.
     *
     * @return Store object or default Store if not found
     */
    public static Store getStoreProfile() {
        String sql = "SELECT id, name, address FROM store LIMIT 1";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return new Store(rs.getInt("id"), rs.getString("name"), rs.getString("address"));
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseConnection] Error reading store profile: " + e.getMessage());
        }
        return new Store(1, "PrediStock MSME Store", "Default Address");
    }

    /**
     * Helper to update Store profile in SQLite.
     *
     * @param name    new store name
     * @param address new store address
     */
    public static void updateStoreProfile(String name, String address) {
        String sql = "UPDATE store SET name = ?, address = ? WHERE id = 1";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, address);
            int updated = pstmt.executeUpdate();
            if (updated == 0) {
                // Insert if not yet exists
                String insertSql = "INSERT INTO store (id, name, address) VALUES (1, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setString(1, name);
                    insertStmt.setString(2, address);
                    insertStmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseConnection] Error updating store profile: " + e.getMessage());
        }
    }
}

