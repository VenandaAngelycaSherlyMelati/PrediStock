package com.predistock.repository;

import com.predistock.model.FoodProduct;
import com.predistock.model.GoodsProduct;
import com.predistock.model.Product;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository implementation for Product entities using raw JDBC.
 * Demonstrates:
 * - Implementation of Generic Interface (Repository<Product>)
 * - Polymorphic Mapping (Dynamically maps database rows to FoodProduct or GoodsProduct)
 * - Raw JDBC operations with PreparedStatement and ResultSet (No JPA/Hibernate)
 */
@Repository
public class ProductRepository implements com.predistock.repository.Repository<Product> {

    @Override
    public void save(Product product) {
        String sql = """
                INSERT INTO products (name, price, stock, min_stock, type, expired_date, brand)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, product.getName());
            pstmt.setDouble(2, product.getPrice());
            pstmt.setInt(3, product.getStock());
            pstmt.setInt(4, product.getMinStock());
            pstmt.setString(5, product.getCategory());

            if (product instanceof FoodProduct food) {
                pstmt.setString(6, food.getExpiredDate().toString());
                pstmt.setNull(7, Types.VARCHAR);
            } else if (product instanceof GoodsProduct goods) {
                pstmt.setNull(6, Types.VARCHAR);
                pstmt.setString(7, goods.getBrand());
            } else {
                pstmt.setNull(6, Types.VARCHAR);
                pstmt.setNull(7, Types.VARCHAR);
            }

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        product.setId(generatedKeys.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving product to database: " + e.getMessage(), e);
        }
    }

    @Override
    public Product findById(int id) {
        String sql = "SELECT id, name, price, stock, min_stock, type, expired_date, brand FROM products WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToProduct(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding product by id: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<Product> findAll() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT id, name, price, stock, min_stock, type, expired_date, brand FROM products ORDER BY id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToProduct(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving all products: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public void update(Product product) {
        String sql = """
                UPDATE products
                SET name = ?, price = ?, stock = ?, min_stock = ?, type = ?, expired_date = ?, brand = ?
                WHERE id = ?
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, product.getName());
            pstmt.setDouble(2, product.getPrice());
            pstmt.setInt(3, product.getStock());
            pstmt.setInt(4, product.getMinStock());
            pstmt.setString(5, product.getCategory());

            if (product instanceof FoodProduct food) {
                pstmt.setString(6, food.getExpiredDate().toString());
                pstmt.setNull(7, Types.VARCHAR);
            } else if (product instanceof GoodsProduct goods) {
                pstmt.setNull(6, Types.VARCHAR);
                pstmt.setString(7, goods.getBrand());
            } else {
                pstmt.setNull(6, Types.VARCHAR);
                pstmt.setNull(7, Types.VARCHAR);
            }
            pstmt.setInt(8, product.getId());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating product in database: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting product: " + e.getMessage(), e);
        }
    }

    /**
     * Feature B-04: Queries products where stock <= min_stock.
     *
     * @return list of products with depleted inventory
     */
    public List<Product> findLowStock() {
        List<Product> list = new ArrayList<>();
        String sql = """
                SELECT id, name, price, stock, min_stock, type, expired_date, brand
                FROM products
                WHERE stock <= min_stock
                ORDER BY stock ASC
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToProduct(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving low stock products: " + e.getMessage(), e);
        }
        return list;
    }

    /**
     * Feature B-05: Queries food products that expire within N days or have expired.
     *
     * @param days warning days window
     * @return list of FoodProduct objects
     */
    public List<FoodProduct> findNearExpired(int days) {
        List<FoodProduct> list = new ArrayList<>();
        String thresholdDate = LocalDate.now().plusDays(days).toString();
        String sql = """
                SELECT id, name, price, stock, min_stock, type, expired_date, brand
                FROM products
                WHERE type = 'FOOD' AND expired_date IS NOT NULL AND expired_date <= ?
                ORDER BY expired_date ASC
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, thresholdDate);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Product p = mapRowToProduct(rs);
                    if (p instanceof FoodProduct food) {
                        list.add(food);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving near-expired food products: " + e.getMessage(), e);
        }
        return list;
    }

    /**
     * Core Polymorphic Factory Method:
     * Reads database row and dynamically instantiates FoodProduct or GoodsProduct
     * based on column 'type'.
     */
    private Product mapRowToProduct(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        double price = rs.getDouble("price");
        int stock = rs.getInt("stock");
        int minStock = rs.getInt("min_stock");
        String type = rs.getString("type");
        String expiredDateStr = rs.getString("expired_date");
        String brand = rs.getString("brand");

        if ("FOOD".equalsIgnoreCase(type)) {
            LocalDate expDate = (expiredDateStr != null && !expiredDateStr.trim().isEmpty())
                    ? LocalDate.parse(expiredDateStr)
                    : LocalDate.now().plusDays(30);
            return new FoodProduct(id, name, price, stock, minStock, expDate);
        } else {
            return new GoodsProduct(id, name, price, stock, minStock, brand);
        }
    }
}

