package com.predistock.controller;

import com.predistock.exception.InsufficientStockException;
import com.predistock.exception.ProductNotFoundException;
import com.predistock.model.FoodProduct;
import com.predistock.model.GoodsProduct;
import com.predistock.model.Product;
import com.predistock.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller for Product Management and Inventory Operations.
 * Strictly implements the REST endpoints outlined in Section 2.10 of the proposal.
 */
@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * GET /api/products
     * Retrieves all products in the catalog with polymorphic attributes.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> handleList() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Map<String, Object>> products = productService.getAllProducts().stream()
                    .map(this::convertProductToMap)
                    .collect(Collectors.toList());

            response.put("success", true);
            response.put("data", products);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Gagal memuat produk: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * GET /api/products/{id}
     * Retrieves single product by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> handleGetById(@PathVariable("id") int id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Product product = productService.getProductById(id);
            response.put("success", true);
            response.put("data", convertProductToMap(product));
            return ResponseEntity.ok(response);
        } catch (ProductNotFoundException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * POST /api/products
     * Creates a new FoodProduct or GoodsProduct (Owner only).
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> handleCreate(@RequestBody CreateProductRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Product product;
            String type = request.type() != null ? request.type().trim().toUpperCase() : "GOODS";

            if ("FOOD".equals(type)) {
                LocalDate expDate = request.expiredDate() != null
                        ? LocalDate.parse(request.expiredDate())
                        : LocalDate.now().plusDays(30);
                product = new FoodProduct(
                        request.name(),
                        request.price(),
                        request.stock(),
                        request.minStock(),
                        expDate
                );
            } else {
                product = new GoodsProduct(
                        request.name(),
                        request.price(),
                        request.stock(),
                        request.minStock(),
                        request.brand() != null ? request.brand() : "Umum"
                );
            }

            productService.addProduct(product);

            response.put("success", true);
            response.put("message", "Produk '" + product.getName() + "' berhasil ditambahkan");
            response.put("data", convertProductToMap(product));
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * PUT /api/products/{id}
     * Updates an existing product (Owner only).
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> handleUpdate(
            @PathVariable("id") int id,
            @RequestBody CreateProductRequest request) {

        Map<String, Object> response = new HashMap<>();
        try {
            Product existing = productService.getProductById(id);
            Product updated;

            String type = request.type() != null ? request.type().trim().toUpperCase() : existing.getCategory();
            String name = (request.name() != null && !request.name().trim().isEmpty()) ? request.name().trim() : existing.getName();
            double price = request.price() > 0 ? request.price() : existing.getPrice();
            int stock = request.stock() >= 0 ? request.stock() : existing.getStock();
            int minStock = request.minStock() >= 0 ? request.minStock() : existing.getMinStock();

            if ("FOOD".equals(type)) {
                LocalDate expDate = request.expiredDate() != null
                        ? LocalDate.parse(request.expiredDate())
                        : (existing instanceof FoodProduct food ? food.getExpiredDate() : LocalDate.now().plusDays(30));
                updated = new FoodProduct(id, name, price, stock, minStock, expDate);
            } else {
                String brand = request.brand() != null
                        ? request.brand()
                        : (existing instanceof GoodsProduct goods ? goods.getBrand() : "Umum");
                updated = new GoodsProduct(id, name, price, stock, minStock, brand);
            }

            productService.updateProduct(updated);

            response.put("success", true);
            response.put("message", "Produk '" + updated.getName() + "' berhasil diperbarui");
            response.put("data", convertProductToMap(updated));
            return ResponseEntity.ok(response);

        } catch (ProductNotFoundException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Gagal memperbarui produk: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * DELETE /api/products/{id}
     * Deletes a product by ID (Owner only).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> handleDelete(@PathVariable("id") int id) {
        Map<String, Object> response = new HashMap<>();
        try {
            productService.deleteProduct(id);
            response.put("success", true);
            response.put("message", "Produk berhasil dihapus dari katalog");
            return ResponseEntity.ok(response);
        } catch (ProductNotFoundException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Gagal menghapus produk: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * POST /api/products/{id}/restock
     * Adds inventory stock for a product (Feature B-02).
     */
    @PostMapping("/{id}/restock")
    public ResponseEntity<Map<String, Object>> handleRestock(
            @PathVariable("id") int id,
            @RequestBody RestockRequest request) {

        Map<String, Object> response = new HashMap<>();
        try {
            Product product = productService.restock(id, request.qty());
            response.put("success", true);
            response.put("message", "Stok berhasil ditambahkan. Stok sekarang: " + product.getStock());
            response.put("data", convertProductToMap(product));
            return ResponseEntity.ok(response);

        } catch (ProductNotFoundException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * GET /api/products/alerts
     * Retrieves inventory warnings: low stock and perishable expiration alerts.
     */
    @GetMapping("/alerts")
    public ResponseEntity<Map<String, Object>> handleAlerts(
            @RequestParam(name = "days", defaultValue = "3") int days) {

        Map<String, Object> response = new HashMap<>();
        try {
            List<Map<String, Object>> lowStockList = productService.getLowStockProducts().stream()
                    .map(this::convertProductToMap)
                    .collect(Collectors.toList());

            List<Map<String, Object>> nearExpiredList = productService.getNearExpiredProducts(days).stream()
                    .map(this::convertProductToMap)
                    .collect(Collectors.toList());

            Map<String, Object> alertsData = new HashMap<>();
            alertsData.put("lowStockCount", lowStockList.size());
            alertsData.put("lowStockProducts", lowStockList);
            alertsData.put("nearExpiredCount", nearExpiredList.size());
            alertsData.put("nearExpiredProducts", nearExpiredList);

            response.put("success", true);
            response.put("data", alertsData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Gagal memuat alerts: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Converts a domain Product to a JSON-ready Map.
     * Demonstrates dynamic polymorphism by calling getStorageInfo() and getCategory().
     */
    private Map<String, Object> convertProductToMap(Product p) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", p.getId());
        map.put("name", p.getName());
        map.put("price", p.getPrice());
        map.put("stock", p.getStock());
        map.put("minStock", p.getMinStock());
        map.put("isLowStock", p.isLowStock());
        map.put("category", p.getCategory());         // Polymorphism
        map.put("storageInfo", p.getStorageInfo());   // Polymorphism

        if (p instanceof FoodProduct food) {
            map.put("expiredDate", food.getExpiredDate().toString());
            map.put("isExpired", food.isExpired());
            map.put("isNearExpired", food.isNearExpired(3));
        } else if (p instanceof GoodsProduct goods) {
            map.put("brand", goods.getBrand());
        }

        return map;
    }

    // DTO records
    public record CreateProductRequest(
            String name,
            double price,
            int stock,
            int minStock,
            String type,        // "FOOD" or "GOODS"
            String expiredDate, // "YYYY-MM-DD"
            String brand        // for GOODS
    ) {}

    public record RestockRequest(int qty) {}
}

