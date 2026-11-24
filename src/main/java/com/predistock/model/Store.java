package com.predistock.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Model representing Store Profile and Aggregation of users and products.
 * Demonstrates:
 * - Aggregation (Store o-- User, Store o-- Product)
 * - Encapsulation of store metadata
 */
public class Store {
    private int id;
    private String name;
    private String address;
    private List<User> users;
    private List<Product> products;

    public Store() {
        this.users = new ArrayList<>();
        this.products = new ArrayList<>();
    }

    public Store(int id, String name, String address) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.users = new ArrayList<>();
        this.products = new ArrayList<>();
    }

    public Store(String name, String address) {
        this(0, name, address);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    /**
     * Demonstrates aggregation: adding a User reference to the Store.
     *
     * @param u User to add
     */
    public void addUser(User u) {
        if (u != null && !this.users.contains(u)) {
            this.users.add(u);
        }
    }

    /**
     * Demonstrates aggregation: adding a Product reference to the Store.
     *
     * @param p Product to add
     */
    public void addProduct(Product p) {
        if (p != null && !this.products.contains(p)) {
            this.products.add(p);
        }
    }

    @Override
    public String toString() {
        return "Store{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", totalUsers=" + users.size() +
                ", totalProducts=" + products.size() +
                '}';
    }
}
