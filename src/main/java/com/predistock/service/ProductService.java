package com.predistock.service;

import com.predistock.exception.ProductNotFoundException;
import com.predistock.model.FoodProduct;
import com.predistock.model.Product;
import com.predistock.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service orchestrating product management, stock changes, and expiry alerts.
 * Strictly adheres to Clean Architecture: Controller -> Service -> Repository.
 */
@Service
public class ProductService {

    private final ProductRepository productRepo;

    public ProductService(ProductRepository productRepo) {
        this.productRepo = productRepo;
    }

    /**
     * B-01: Adds a new product to inventory.
     *
     * @param p Product instance (FoodProduct or GoodsProduct)
     */
    public void addProduct(Product p) {
        if (p == null) {
            throw new IllegalArgumentException("Produk tidak boleh null");
        }
        productRepo.save(p);
    }

    /**
     * B-01: Updates existing product metadata.
     *
     * @param p Product instance with updated fields
     */
    public void updateProduct(Product p) {
        if (p == null) {
            throw new IllegalArgumentException("Produk tidak boleh null");
        }
        Product existing = productRepo.findById(p.getId());
        if (existing == null) {
            throw new ProductNotFoundException("Produk dengan ID " + p.getId() + " tidak ditemukan");
        }
        productRepo.update(p);
    }

    /**
     * B-01: Deletes product by ID.
     *
     * @param id primary key of product
     */
    public void deleteProduct(int id) {
        Product existing = productRepo.findById(id);
        if (existing == null) {
            throw new ProductNotFoundException("Produk dengan ID " + id + " tidak ditemukan");
        }
        productRepo.delete(id);
    }

    /**
     * B-02: Restocks an existing product (increases available inventory).
     * Demonstrates encapsulation by delegating stock increase to Product.addStock().
     *
     * @param id  product ID
     * @param qty quantity of stock to add
     * @return updated Product instance
     */
    public Product restock(int id, int qty) {
        Product product = productRepo.findById(id);
        if (product == null) {
            throw new ProductNotFoundException("Produk dengan ID " + id + " tidak ditemukan");
        }
        product.addStock(qty);
        productRepo.update(product);
        return product;
    }

    /**
     * B-03 Helper: Safely reduces stock by invoking Product.reduceStock(qty).
     *
     * @param id  product ID
     * @param qty quantity of items to deduct
     * @return updated Product instance
     */
    public Product reduceStock(int id, int qty) {
        Product product = productRepo.findById(id);
        if (product == null) {
            throw new ProductNotFoundException("Produk dengan ID " + id + " tidak ditemukan");
        }
        product.reduceStock(qty);
        productRepo.update(product);
        return product;
    }

    /**
     * Retrieves all products.
     *
     * @return list of all products (Polymorphic: FoodProduct and GoodsProduct)
     */
    public List<Product> getAllProducts() {
        return productRepo.findAll();
    }

    /**
     * Finds single product by ID.
     *
     * @param id product ID
     * @return Product object
     */
    public Product getProductById(int id) {
        Product product = productRepo.findById(id);
        if (product == null) {
            throw new ProductNotFoundException("Produk dengan ID " + id + " tidak ditemukan");
        }
        return product;
    }

    /**
     * B-04: Retrieves products with low inventory level (stock <= min_stock).
     *
     * @return list of low-stock products
     */
    public List<Product> getLowStockProducts() {
        return productRepo.findLowStock();
    }

    /**
     * B-05: Retrieves perishable food items that are near expiration.
     *
     * @param days warning days window
     * @return list of FoodProduct objects
     */
    public List<FoodProduct> getNearExpiredProducts(int days) {
        return productRepo.findNearExpired(days);
    }
}

