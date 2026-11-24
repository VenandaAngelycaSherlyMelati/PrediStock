package com.predistock.forecast;

import java.time.LocalDate;

/**
 * Model class holding a single forecast data point (a future calendar date and the predicted quantity).
 */
public class ForecastResult {
    private LocalDate date;
    private double predictedQty;

    public ForecastResult(LocalDate date, double predictedQty) {
        this.date = date;
        this.predictedQty = Math.max(0.0, predictedQty); // Negative demand is physically impossible
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public double getPredictedQty() {
        return predictedQty;
    }

    public void setPredictedQty(double predictedQty) {
        this.predictedQty = Math.max(0.0, predictedQty);
    }

    @Override
    public String toString() {
        return "ForecastResult{" +
                "date=" + date +
                ", predictedQty=" + String.format("%.2f", predictedQty) +
                '}';
    }
}

