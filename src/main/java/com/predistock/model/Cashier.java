package com.predistock.model;

/**
 * Concrete class representing a Cashier (Kasir).
 * Inherits from User and restricts administrative and reporting privileges.
 * Demonstrates:
 * - Inheritance (extends User)
 * - Polymorphism (Custom role behavior & access restrictions)
 * - Encapsulation (shiftNumber with getters/setters)
 */
public class Cashier extends User {
    private int shiftNumber;

    public Cashier(int id, String username, String passwordHash, String name, int shiftNumber) {
        super(id, username, passwordHash, name);
        this.shiftNumber = shiftNumber;
    }

    public Cashier(String username, String passwordHash, String name, int shiftNumber) {
        super(username, passwordHash, name);
        this.shiftNumber = shiftNumber;
    }

    public Cashier(int id, String username, String passwordHash, String name) {
        this(id, username, passwordHash, name, 1);
    }

    public Cashier(String username, String passwordHash, String name) {
        this(0, username, passwordHash, name, 1);
    }

    @Override
    public String getRole() {
        return "CASHIER";
    }

    @Override
    public boolean canManageProducts() {
        return false;
    }

    @Override
    public boolean canAddProduct() {
        return false;
    }

    @Override
    public boolean canDeleteProduct() {
        return false;
    }

    @Override
    public boolean canRestock() {
        return true; // Kasir diperbolehkan melakukan restok barang masuk
    }

    @Override
    public boolean canEditPrice() {
        return false; // Kasir dilarang mengubah harga jual produk
    }

    @Override
    public boolean canViewReports() {
        return false;
    }

    @Override
    public boolean canResetDatabase() {
        return false;
    }

    public int getShiftNumber() {
        return shiftNumber;
    }

    public void setShiftNumber(int shiftNumber) {
        this.shiftNumber = shiftNumber;
    }

    @Override
    public String toString() {
        return "Cashier{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", name='" + name + '\'' +
                ", role='" + getRole() + '\'' +
                ", shiftNumber=" + shiftNumber +
                '}';
    }
}

