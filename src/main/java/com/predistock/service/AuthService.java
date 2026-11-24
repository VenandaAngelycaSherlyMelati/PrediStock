package com.predistock.service;

import com.predistock.exception.InvalidLoginException;
import com.predistock.model.Cashier;
import com.predistock.model.Store;
import com.predistock.model.User;
import com.predistock.repository.DatabaseConnection;
import com.predistock.repository.UserRepository;
import com.predistock.util.PasswordUtil;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service handling authentication, user administration, and store profile management.
 * Strictly adheres to Clean Architecture: Controller -> Service -> Repository.
 */
@Service
public class AuthService {

    private final UserRepository userRepo;

    public AuthService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    /**
     * Authenticates a user given username and plain-text password.
     *
     * @param username user credential
     * @param password raw plain-text password
     * @return authenticated User (polymorphic instance: Owner or Cashier)
     * @throws InvalidLoginException if credentials do not match
     */
    public User login(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            throw new InvalidLoginException("Username dan password tidak boleh kosong");
        }

        User user = userRepo.findByUsername(username.trim());
        if (user == null) {
            throw new InvalidLoginException("Username atau password salah");
        }

        // Object-Oriented verification delegating to User model encapsulation
        if (!user.checkPassword(password)) {
            throw new InvalidLoginException("Username atau password salah");
        }

        return user;
    }

    /**
     * Registers a new user into the database.
     *
     * @param user user entity to persist
     */
    public void register(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User tidak boleh null");
        }
        User existing = userRepo.findByUsername(user.getUsername());
        if (existing != null) {
            throw new IllegalArgumentException("Username '" + user.getUsername() + "' sudah digunakan");
        }
        userRepo.save(user);
    }

    /**
     * Creates a new Cashier account (A-03: Manajemen akun kasir).
     *
     * @param username    unique username
     * @param rawPassword plain-text password to hash
     * @param name        cashier's full name
     * @param shiftNumber shift number
     * @return newly created Cashier object
     */
    public Cashier createCashier(String username, String rawPassword, String name, int shiftNumber) {
        String hashedPassword = PasswordUtil.hash(rawPassword);
        Cashier cashier = new Cashier(0, username, hashedPassword, name, shiftNumber);
        register(cashier);
        return cashier;
    }

    /**
     * Updates an existing Cashier account (A-03: Manajemen akun kasir).
     *
     * @param id          cashier user ID
     * @param name        updated full name
     * @param shiftNumber updated shift number
     * @param newPassword optional new password (plain-text, hashed if provided)
     * @return updated Cashier object
     */
    public Cashier updateCashier(int id, String name, int shiftNumber, String newPassword) {
        User user = userRepo.findById(id);
        if (user == null) {
            throw new IllegalArgumentException("Kasir dengan ID " + id + " tidak ditemukan");
        }
        if (!(user instanceof Cashier cashier)) {
            throw new IllegalArgumentException("User dengan ID " + id + " bukan akun kasir");
        }

        if (name != null && !name.trim().isEmpty()) {
            cashier.setName(name.trim());
        }
        if (shiftNumber > 0) {
            cashier.setShiftNumber(shiftNumber);
        }
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            cashier.setPasswordHash(PasswordUtil.hash(newPassword.trim()));
        }

        userRepo.update(cashier);
        return cashier;
    }

    /**
     * Retrieves all registered users.
     *
     * @return list of User entities
     */
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    /**
     * Deletes a user by ID.
     *
     * @param id user primary key
     */
    public void deleteUser(int id) {
        userRepo.delete(id);
    }

    /**
     * Retrieves store profile (A-04: Profil toko).
     */
    public Store getStore() {
        Store store = DatabaseConnection.getStoreProfile();
        store.setUsers(userRepo.findAll());
        return store;
    }

    /**
     * Updates store profile (A-04: Profil toko).
     */
    public void updateStore(String name, String address) {
        DatabaseConnection.updateStoreProfile(name, address);
    }

    /**
     * Handles logout event.
     */
    public void logout() {
        // Stateless REST API logout acknowledgment; can be extended for session tracking
        System.out.println("[AuthService] User session ended.");
    }
}

