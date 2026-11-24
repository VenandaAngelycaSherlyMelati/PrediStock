package com.predistock.controller;

import com.predistock.discount.NominalDiscount;
import com.predistock.discount.PercentDiscount;
import com.predistock.exception.InsufficientStockException;
import com.predistock.exception.ProductNotFoundException;
import com.predistock.model.Cashier;
import com.predistock.model.Product;
import com.predistock.model.Transaction;
import com.predistock.model.TransactionItem;
import com.predistock.model.User;
import com.predistock.repository.UserRepository;
import com.predistock.service.ProductService;
import com.predistock.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller for Point of Sale Sales Transactions and Receipts.
 * Strictly implements the REST endpoints outlined in Section 2.10 of the proposal.
 */
@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {

    private final TransactionService transactionService;
    private final ProductService productService;
    private final UserRepository userRepository;

    public TransactionController(TransactionService transactionService,
                                 ProductService productService,
                                 UserRepository userRepository) {
        this.transactionService = transactionService;
        this.productService = productService;
        this.userRepository = userRepository;
    }

    /**
     * POST /api/transactions
     * Checkout endpoint: processes sale, applies discount polymorphism, deducts stock, and saves transaction.
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> handleCheckout(@RequestBody CheckoutRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (request.items() == null || request.items().isEmpty()) {
                response.put("success", false);
                response.put("message", "Transaksi harus memiliki minimal 1 item produk");
                return ResponseEntity.badRequest().body(response);
            }

            // 1. Resolve cashier
            int cashierId = request.cashierId() != null ? request.cashierId() : 1;
            User user = userRepository.findById(cashierId);
            Cashier cashier = (user instanceof Cashier c) ? c : new Cashier(cashierId, "kasir", "", "Kasir Toko", 1);

            // 2. Build Transaction model (Composition)
            Transaction transaction = new Transaction(0, cashier, LocalDateTime.now());

            for (CheckoutItemRequest itemReq : request.items()) {
                Product product = productService.getProductById(itemReq.productId());
                transaction.addItem(product, itemReq.qty());
            }

            // 3. Apply Polymorphic Discount Strategy
            if (request.discountType() != null && request.discountValue() != null && request.discountValue() > 0) {
                if ("PERCENT".equalsIgnoreCase(request.discountType())) {
                    transaction.setDiscount(new PercentDiscount(request.discountValue()));
                } else if ("NOMINAL".equalsIgnoreCase(request.discountType())) {
                    transaction.setDiscount(new NominalDiscount(request.discountValue()));
                }
            }

            // 4. Execute Checkout Workflow in Service Layer
            transactionService.checkout(transaction);

            // 5. Calculate Payment & Change if paid amount is provided
            double paid = request.paid() != null ? request.paid() : transaction.calculateTotal();
            double change = transaction.calculateChange(paid);
            String receipt = transaction.generateReceipt(paid);

            Map<String, Object> data = convertTransactionToMap(transaction);
            data.put("paid", paid);
            data.put("change", change);
            data.put("receipt", receipt);

            response.put("success", true);
            response.put("message", "Transaksi berhasil disimpan");
            response.put("data", data);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (InsufficientStockException e) {
            response.put("success", false);
            response.put("error", "INSUFFICIENT_STOCK");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (ProductNotFoundException e) {
            response.put("success", false);
            response.put("error", "PRODUCT_NOT_FOUND");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Terjadi kesalahan internal: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * GET /api/transactions
     * Retrieves transaction history (with optional date range filtering).
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> handleHistory(
            @RequestParam(name = "from", required = false) String from,
            @RequestParam(name = "to", required = false) String to) {

        Map<String, Object> response = new HashMap<>();
        try {
            List<Transaction> list;
            if (from != null && to != null) {
                list = transactionService.getHistoryByDateRange(LocalDate.parse(from), LocalDate.parse(to));
            } else {
                list = transactionService.getHistory();
            }

            List<Map<String, Object>> result = list.stream()
                    .map(this::convertTransactionToMap)
                    .collect(Collectors.toList());

            response.put("success", true);
            response.put("data", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Gagal memuat riwayat transaksi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * GET /api/transactions/{id}
     * Retrieves details for a specific transaction.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> handleGetById(@PathVariable("id") int id) {
        Transaction trx = transactionService.getTransactionById(id);
        Map<String, Object> response = new HashMap<>();
        if (trx == null) {
            response.put("success", false);
            response.put("message", "Transaksi ID " + id + " tidak ditemukan");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        response.put("success", true);
        response.put("data", convertTransactionToMap(trx));
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/transactions/{id}/receipt
     * Retrieves plain-text printable receipt string (Feature C-04).
     */
    @GetMapping("/{id}/receipt")
    public ResponseEntity<Map<String, Object>> handleReceipt(
            @PathVariable("id") int id,
            @RequestParam(name = "paid", required = false) Double paid) {

        Map<String, Object> response = new HashMap<>();
        try {
            String receipt = (paid != null)
                    ? transactionService.getReceipt(id, paid)
                    : transactionService.getReceipt(id);

            response.put("success", true);
            response.put("receipt", receipt);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    private Map<String, Object> convertTransactionToMap(Transaction trx) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", trx.getId());
        map.put("date", trx.getDate().toString());

        String cashierName = trx.getCashier() != null ? trx.getCashier().getName() : "Kasir";
        map.put("cashierName", cashierName);
        Map<String, Object> cashierMap = new HashMap<>();
        cashierMap.put("id", trx.getCashier() != null ? trx.getCashier().getId() : 1);
        cashierMap.put("name", cashierName);
        cashierMap.put("username", trx.getCashier() != null ? trx.getCashier().getUsername() : "kasir");
        cashierMap.put("role", trx.getCashier() != null ? trx.getCashier().getRole() : "CASHIER");
        map.put("cashier", cashierMap);

        map.put("subtotal", trx.calculateSubtotal());
        String discountDesc = trx.getDiscount() != null ? trx.getDiscount().getDescription() : "-";
        map.put("discountDescription", discountDesc);
        if (trx.getDiscount() != null) {
            Map<String, Object> discountMap = new HashMap<>();
            discountMap.put("description", discountDesc);
            map.put("discount", discountMap);
        } else {
            map.put("discount", null);
        }
        map.put("total", trx.calculateTotal());

        List<Map<String, Object>> itemsList = trx.getItems().stream().map(item -> {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("productId", item.getProduct().getId());
            itemMap.put("productName", item.getProduct().getName());

            Map<String, Object> productMap = new HashMap<>();
            productMap.put("id", item.getProduct().getId());
            productMap.put("name", item.getProduct().getName());
            productMap.put("price", item.getProduct().getPrice());
            productMap.put("category", item.getProduct().getCategory());
            itemMap.put("product", productMap);

            itemMap.put("category", item.getProduct().getCategory());
            itemMap.put("qty", item.getQty());
            itemMap.put("unitPrice", item.getUnitPrice());
            itemMap.put("subtotal", item.getSubtotal());
            return itemMap;
        }).collect(Collectors.toList());

        map.put("items", itemsList);
        return map;
    }

    // DTO records
    public record CheckoutRequest(
            Integer cashierId,
            List<CheckoutItemRequest> items,
            String discountType,    // "PERCENT" or "NOMINAL"
            Double discountValue,
            Double paid
    ) {}

    public record CheckoutItemRequest(int productId, int qty) {}
}

