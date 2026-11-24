package com.predistock.model;

/**
 * Concrete class representing non-perishable general consumer goods.
 * Demonstrates:
 * - Inheritance (extends Product)
 * - Polymorphism (Overrides getStorageInfo(), getCategory())
 * - Encapsulation (Brand attribute with getter/setter)
 */
public class GoodsProduct extends Product {
    private String brand;

    public GoodsProduct(int id, String name, double price, int stock, int minStock, String brand) {
        super(id, name, price, stock, minStock);
        this.brand = (brand != null && !brand.trim().isEmpty()) ? brand.trim() : "Generic";
    }

    public GoodsProduct(String name, double price, int stock, int minStock, String brand) {
        this(0, name, price, stock, minStock, brand);
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = (brand != null && !brand.trim().isEmpty()) ? brand.trim() : "Generic";
    }

    @Override
    public String getStorageInfo() {
        return "Simpan di rak barang pada suhu ruangan. Merek: " + brand;
    }

    @Override
    public String getCategory() {
        return "GOODS";
    }

    @Override
    public String toString() {
        return "GoodsProduct{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", stock=" + stock +
                ", minStock=" + minStock +
                ", brand='" + brand + '\'' +
                '}';
    }
}

