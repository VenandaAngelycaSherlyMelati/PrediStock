package com.predistock.forecast;

import com.predistock.model.DailySales;
import com.predistock.util.MathUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class defining the blueprint for all manual time-series forecasting algorithms.
 * Demonstrates:
 * - Abstract Class & Abstract Methods (forecast(), getMethodName())
 * - Protected Shared State (#history)
 * - Template logic for MAPE error computation and 7-day inventory restock recommendation
 * - Pure manual mathematical logic (NO ML libraries)
 */
public abstract class Forecaster {
    protected List<DailySales> history;

    public Forecaster(List<DailySales> history) {
        if (history == null || history.isEmpty()) {
            throw new IllegalArgumentException("Data historis penjualan tidak boleh kosong");
        }
        this.history = new ArrayList<>(history);
    }

    public List<DailySales> getHistory() {
        return history;
    }

    /**
     * Abstract method to compute future sales forecasts for N days.
     * Concrete subclasses (MovingAverageForecaster, LinearTrendForecaster) must implement this.
     *
     * @param days number of future days to forecast
     * @return list of ForecastResult
     */
    public abstract List<ForecastResult> forecast(int days);

    /**
     * Abstract method to obtain the human-readable method title.
     *
     * @return method name
     */
    public abstract String getMethodName();

    /**
     * In-sample accuracy measurement using Mean Absolute Percentage Error (MAPE).
     * Compares model backtest predictions against actual historical values.
     *
     * @return MAPE percentage (e.g. 5.42%)
     */
    public abstract double evaluateHistoricalMape();

    /**
     * Formula Bab 2.7 (c):
     * MAPE = (100% / n) * \Sigma (|y_aktual - y_prediksi| / y_aktual)
     * Days where y_aktual == 0 are skipped to avoid division by zero.
     *
     * @param actual    actual recorded historical quantities
     * @param predicted corresponding predicted quantities
     * @return MAPE percentage
     */
    public double calculateMape(List<Double> actual, List<Double> predicted) {
        if (actual == null || predicted == null || actual.size() != predicted.size() || actual.isEmpty()) {
            return 0.0;
        }

        double sumPercentageError = 0.0;
        int validCount = 0;

        for (int i = 0; i < actual.size(); i++) {
            double yActual = actual.get(i);
            double yPred = predicted.get(i);

            if (yActual > 0) { // Avoid division by zero
                double error = Math.abs(yActual - yPred) / yActual;
                sumPercentageError += error;
                validCount++;
            }
        }

        if (validCount == 0) {
            return 0.0;
        }

        double mape = (100.0 / validCount) * sumPercentageError;
        return MathUtil.round(mape, 2);
    }

    /**
     * Formula Bab 2.7 (d):
     * jumlah restok = \Sigma prediksi 7 hari + stok pengaman - stok saat ini
     * stok pengaman = 20% * \Sigma prediksi 7 hari
     * (jika hasilnya negatif, rekomendasi = 0)
     *
     * @param currentStock current inventory on hand
     * @param days         projection horizon (typically 7 days)
     * @return recommended units to restock
     */
    public int recommendRestock(int currentStock, int days) {
        List<ForecastResult> results = forecast(days);
        double sumPredicted = 0.0;
        for (ForecastResult res : results) {
            sumPredicted += res.getPredictedQty();
        }

        double safetyStock = 0.20 * sumPredicted;
        double needed = (sumPredicted + safetyStock) - currentStock;

        int restockQty = (int) Math.round(needed);
        return Math.max(0, restockQty);
    }
}

