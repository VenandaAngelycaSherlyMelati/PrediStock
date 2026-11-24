package com.predistock.repository;

import com.predistock.model.Cashier;
import com.predistock.model.Owner;
import com.predistock.model.User;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository implementation for User entities using raw JDBC.
 * Demonstrates:
 * - Implementation of Generic Interface (Repository<User>)
 * - Polymorphic Instantiation (Maps DB role string to concrete Owner or Cashier instance)
 * - Raw JDBC operations with PreparedStatement and ResultSet (No JPA/Hibernate)
 */
@Repository
public class UserRepository implements com.predistock.repository.Repository<User> {

    @Override
    public void save(User user) {
        String sql = "INSERT INTO users (username, password_hash, name, role, shift_number) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPasswordHash());
            pstmt.setString(3, user.getName());
            pstmt.setString(4, user.getRole());

            if (user instanceof Cashier cashier) {
                pstmt.setInt(5, cashier.getShiftNumber());
            } else {
                pstmt.setInt(5, 1);
            }

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving user to database: " + e.getMessage(), e);
        }
    }

    @Override
    public User findById(int id) {
        String sql = "SELECT id, username, password_hash, name, role, shift_number FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by id: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT id, username, password_hash, name, role, shift_number FROM users ORDER BY id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToUser(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving all users: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public void update(User user) {
        String sql = "UPDATE users SET username = ?, password_hash = ?, name = ?, role = ?, shift_number = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPasswordHash());
            pstmt.setString(3, user.getName());
            pstmt.setString(4, user.getRole());

            if (user instanceof Cashier cashier) {
                pstmt.setInt(5, cashier.getShiftNumber());
            } else {
                pstmt.setInt(5, 1);
            }
            pstmt.setInt(6, user.getId());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating user in database: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting user: " + e.getMessage(), e);
        }
    }

    /**
     * Custom finder by username.
     *
     * @param username user login name
     * @return User object (Owner or Cashier) or null if not found
     */
    public User findByUsername(String username) {
        String sql = "SELECT id, username, password_hash, name, role, shift_number FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by username: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Core Polymorphic Factory Method:
     * Reads database row and dynamically constructs the specific concrete User subclass
     * (Owner or Cashier) based on the database column 'role'.
     */
    private User mapRowToUser(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String username = rs.getString("username");
        String passwordHash = rs.getString("password_hash");
        String name = rs.getString("name");
        String role = rs.getString("role");
        int shiftNumber = rs.getInt("shift_number");

        if ("OWNER".equalsIgnoreCase(role)) {
            return new Owner(id, username, passwordHash, name);
        } else {
            return new Cashier(id, username, passwordHash, name, shiftNumber);
        }
    }
}

