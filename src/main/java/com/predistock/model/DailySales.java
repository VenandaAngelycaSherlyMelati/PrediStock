package com.predistock.model;

import java.time.LocalDate;

/**
 * Model representing aggregated sales quantity of a product on a specific calendar date.
 * Acts as the bridge between transactional sales records and the Forecasting module.
 */
public class DailySales {
    private LocalDate date;
    private int productId;
    private int qtySold;

    public DailySales(LocalDate date, int productId, int qtySold) {
        this.date = date;
        this.productId = productId;
        this.qtySold = qtySold;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getQtySold() {
        return qtySold;
    }

    public void setQtySold(int qtySold) {
        this.qtySold = qtySold;
    }

    @Override
    public String toString() {
        return "DailySales{" +
                "date=" + date +
                ", productId=" + productId +
                ", qtySold=" + qtySold +
                '}';
    }
}

