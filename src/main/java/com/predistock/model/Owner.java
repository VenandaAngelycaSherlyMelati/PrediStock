package com.predistock.model;

/**
 * Concrete class representing the Store Owner (Pemilik).
 * Inherits from User and overrides authorization methods with full privileges.
 * Demonstrates:
 * - Inheritance (extends User)
 * - Polymorphism (Overrides getRole, canManageProducts, canViewReports)
 */
public class Owner extends User {

    public Owner(int id, String username, String passwordHash, String name) {
        super(id, username, passwordHash, name);
    }

    public Owner(String username, String passwordHash, String name) {
        super(username, passwordHash, name);
    }

    @Override
    public String getRole() {
        return "OWNER";
    }

    @Override
    public boolean canManageProducts() {
        return true;
    }

    @Override
    public boolean canAddProduct() {
        return true;
    }

    @Override
    public boolean canDeleteProduct() {
        return true;
    }

    @Override
    public boolean canRestock() {
        return true;
    }

    @Override
    public boolean canEditPrice() {
        return true;
    }

    @Override
    public boolean canViewReports() {
        return true;
    }

    @Override
    public boolean canResetDatabase() {
        return true;
    }
}

