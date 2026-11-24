package com.predistock.service;

import com.predistock.exception.InsufficientStockException;
import com.predistock.exception.ProductNotFoundException;
import com.predistock.model.Product;
import com.predistock.model.Transaction;
import com.predistock.model.TransactionItem;
import com.predistock.repository.ProductRepository;
import com.predistock.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Service managing transactional checkout workflow, stock deduction, and receipt generation.
 * Strictly adheres to Clean Architecture: Controller -> Service -> Repository.
 */
@Service
public class TransactionService {

    private final TransactionRepository trxRepo;
    private final ProductRepository productRepo;

    public TransactionService(TransactionRepository trxRepo, ProductRepository productRepo) {
        this.trxRepo = trxRepo;
        this.productRepo = productRepo;
    }

    /**
     * Executes the checkout process according to Section 2.4 of the proposal:
     * 1. Validates that the transaction has at least one item.
     * 2. Validates available stock for every item; if insufficient, aborts and throws InsufficientStockException.
     * 3. Atomically reduces product stock in memory and updates the database via Product.reduceStock(qty).
     * 4. Persists the transaction and line items to SQLite via TransactionRepository.save().
     *
     * @param transaction Transaction object containing items and optional discount
     */
    public synchronized void checkout(Transaction transaction) {
        if (transaction == null || transaction.getItems().isEmpty()) {
            throw new IllegalArgumentException("Transaksi harus memiliki minimal 1 item produk");
        }

        // Step 1: Pre-check stock availability for all items to guarantee atomic transaction
        for (TransactionItem item : transaction.getItems()) {
            Product currentProduct = productRepo.findById(item.getProduct().getId());
            if (currentProduct == null) {
                throw new ProductNotFoundException("Produk ID " + item.getProduct().getId() + " tidak ditemukan");
            }
            if (currentProduct.getStock() < item.getQty()) {
                throw new InsufficientStockException(String.format(
                        "Stok produk '%s' tidak mencukupi (Stok saat ini: %d, Dibeli: %d)",
                        currentProduct.getName(), currentProduct.getStock(), item.getQty()
                ));
            }
        }

        // Step 2: Deduct stock via OOP method reduceStock() and update database
        for (TransactionItem item : transaction.getItems()) {
            Product product = productRepo.findById(item.getProduct().getId());
            product.reduceStock(item.getQty()); // Throws InsufficientStockException if invalid
            productRepo.update(product);
            // Refresh product instance inside transaction item
            item.setProduct(product);
        }

        // Step 3: Persist transaction and line items to database
        trxRepo.save(transaction);
    }

    /**
     * Retrieves all transaction history sorted by date descending.
     *
     * @return list of transactions
     */
    public List<Transaction> getHistory() {
        return trxRepo.findAll();
    }

    /**
     * Retrieves transaction history filtered by date range.
     *
     * @param from start date
     * @param to   end date
     * @return list of transactions
     */
    public List<Transaction> getHistoryByDateRange(LocalDate from, LocalDate to) {
        if (from == null) from = LocalDate.now().minusMonths(1);
        if (to == null) to = LocalDate.now();
        return trxRepo.findByDateRange(from, to);
    }

    /**
     * Retrieves a transaction by ID.
     *
     * @param id transaction ID
     * @return Transaction object or null
     */
    public Transaction getTransactionById(int id) {
        return trxRepo.findById(id);
    }

    /**
     * Feature C-04: Generates a formatted text receipt for a transaction.
     *
     * @param id transaction ID
     * @return formatted receipt string
     */
    public String getReceipt(int id) {
        Transaction trx = trxRepo.findById(id);
        if (trx == null) {
            throw new IllegalArgumentException("Transaksi ID " + id + " tidak ditemukan");
        }
        return trx.generateReceipt();
    }

    /**
     * Generates a formatted text receipt with customer cash payment and change calculated.
     *
     * @param id   transaction ID
     * @param paid cash received
     * @return formatted receipt string
     */
    public String getReceipt(int id, double paid) {
        Transaction trx = trxRepo.findById(id);
        if (trx == null) {
            throw new IllegalArgumentException("Transaksi ID " + id + " tidak ditemukan");
        }
        return trx.generateReceipt(paid);
    }
}

