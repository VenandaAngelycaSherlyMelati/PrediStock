package com.predistock.controller;

import com.predistock.exception.InvalidLoginException;
import com.predistock.model.Cashier;
import com.predistock.model.Store;
import com.predistock.model.User;
import com.predistock.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller for Authentication, User Administration, and Store Profile.
 * Strictly implements the REST endpoints outlined in Section 2.10 of the proposal.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Allows access from web browser frontend
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /api/auth/login
     * Authenticates user credentials and returns user identity along with polymorphic permissions.
     */
    @PostMapping("/auth/login")
    public ResponseEntity<Map<String, Object>> handleLogin(@RequestBody LoginRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = authService.login(request.username(), request.password());

            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("username", user.getUsername());
            userData.put("name", user.getName());
            userData.put("role", user.getRole()); // Polymorphism: "OWNER" or "CASHIER"
            userData.put("canManageProducts", user.canManageProducts()); // Polymorphism
            userData.put("canAddProduct", user.canAddProduct());         // Polymorphism
            userData.put("canDeleteProduct", user.canDeleteProduct());   // Polymorphism
            userData.put("canRestock", user.canRestock());               // Polymorphism
            userData.put("canEditPrice", user.canEditPrice());           // Polymorphism
            userData.put("canViewReports", user.canViewReports());       // Polymorphism
            userData.put("canResetDatabase", user.canResetDatabase());   // Polymorphism

            if (user instanceof Cashier cashier) {
                userData.put("shiftNumber", cashier.getShiftNumber());
            }

            response.put("success", true);
            response.put("message", "Login berhasil sebagai " + user.getRole());
            response.put("data", userData);
            return ResponseEntity.ok(response);

        } catch (InvalidLoginException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Terjadi kesalahan internal: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * POST /api/auth/logout
     */
    @PostMapping("/auth/logout")
    public ResponseEntity<Map<String, Object>> handleLogout() {
        authService.logout();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Logout berhasil");
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/users
     * Lists all registered users (excluding sensitive password hash).
     */
    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> handleListUsers() {
        List<Map<String, Object>> userList = authService.getAllUsers().stream()
                .map(u -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", u.getId());
                    map.put("username", u.getUsername());
                    map.put("name", u.getName());
                    map.put("role", u.getRole());
                    map.put("canManageProducts", u.canManageProducts());
                    map.put("canAddProduct", u.canAddProduct());
                    map.put("canDeleteProduct", u.canDeleteProduct());
                    map.put("canRestock", u.canRestock());
                    map.put("canEditPrice", u.canEditPrice());
                    map.put("canViewReports", u.canViewReports());
                    map.put("canResetDatabase", u.canResetDatabase());
                    if (u instanceof Cashier c) {
                        map.put("shiftNumber", c.getShiftNumber());
                    }
                    return map;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", userList);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/users
     * Creates a new cashier account (A-03: Owner kelola kasir).
     */
    @PostMapping("/users")
    public ResponseEntity<Map<String, Object>> handleCreateUser(@RequestBody CreateUserRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            int shift = request.shiftNumber() != null ? request.shiftNumber() : 1;
            Cashier created = authService.createCashier(
                    request.username(),
                    request.password(),
                    request.name(),
                    shift
            );

            Map<String, Object> data = new HashMap<>();
            data.put("id", created.getId());
            data.put("username", created.getUsername());
            data.put("name", created.getName());
            data.put("role", created.getRole());
            data.put("shiftNumber", created.getShiftNumber());

            response.put("success", true);
            response.put("message", "Akun kasir berhasil dibuat");
            response.put("data", data);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * PUT /api/users/{id}
     * Updates cashier account (A-03: Manajemen akun kasir - Owner ubah kasir).
     */
    @PutMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> handleUpdateUser(
            @PathVariable("id") int id,
            @RequestBody UpdateUserRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            int shift = request.shiftNumber() != null ? request.shiftNumber() : 1;
            Cashier updated = authService.updateCashier(id, request.name(), shift, request.password());

            Map<String, Object> data = new HashMap<>();
            data.put("id", updated.getId());
            data.put("username", updated.getUsername());
            data.put("name", updated.getName());
            data.put("role", updated.getRole());
            data.put("shiftNumber", updated.getShiftNumber());

            response.put("success", true);
            response.put("message", "Akun kasir '" + updated.getName() + "' berhasil diperbarui");
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Gagal memperbarui kasir: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * DELETE /api/users/{id}
     * Deletes user account.
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> handleDeleteUser(@PathVariable("id") int id) {
        authService.deleteUser(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "User ID " + id + " berhasil dihapus");
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/store
     * Gets store profile (A-04: Profil toko).
     */
    @GetMapping("/store")
    public ResponseEntity<Map<String, Object>> handleGetStore() {
        Store store = authService.getStore();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", Map.of(
                "id", store.getId(),
                "name", store.getName(),
                "address", store.getAddress()
        ));
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/store
     * Updates store profile (A-04: Profil toko).
     */
    @PutMapping("/store")
    public ResponseEntity<Map<String, Object>> handleUpdateStore(@RequestBody StoreRequest request) {
        authService.updateStore(request.name(), request.address());
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Profil toko berhasil diperbarui");
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/init-test-data
     * Resets and populates the database with the complete 14-day test dataset.
     */
    @PostMapping("/init-test-data")
    public ResponseEntity<Map<String, Object>> handleResetTestData() {
        Map<String, Object> response = new HashMap<>();
        try (java.sql.Connection conn = com.predistock.repository.DatabaseConnection.getConnection()) {
            com.predistock.repository.DatabaseConnection.forceResetAndSeed(conn);
            response.put("success", true);
            response.put("message", "Database berhasil di-reset dan diisi ulang dengan data uji coba 14 hari Toko Zaini Sejahtera!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Gagal mereset data: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // DTO records for type safety & clean JSON serialization
    public record LoginRequest(String username, String password) {}
    public record CreateUserRequest(String username, String password, String name, Integer shiftNumber) {}
    public record UpdateUserRequest(String name, Integer shiftNumber, String password) {}
    public record StoreRequest(String name, String address) {}
}

