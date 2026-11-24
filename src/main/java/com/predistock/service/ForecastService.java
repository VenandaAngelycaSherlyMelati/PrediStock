package com.predistock.service;

import com.predistock.exception.InsufficientDataException;
import com.predistock.exception.ProductNotFoundException;
import com.predistock.forecast.ForecastResult;
import com.predistock.forecast.Forecaster;
import com.predistock.forecast.LinearTrendForecaster;
import com.predistock.forecast.MovingAverageForecaster;
import com.predistock.model.DailySales;
import com.predistock.model.Product;
import com.predistock.repository.ProductRepository;
import com.predistock.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service managing statistical forecasting execution, accuracy evaluation (MAPE),
 * and automatic inventory restock recommendations.
 * Strictly adheres to Clean Architecture: Controller -> Service -> Repository.
 */
@Service
public class ForecastService {

    private final TransactionRepository trxRepo;
    private final ProductRepository productRepo;

    public ForecastService(TransactionRepository trxRepo, ProductRepository productRepo) {
        this.trxRepo = trxRepo;
        this.productRepo = productRepo;
    }

    /**
     * Executes both Moving Average and Linear Regression forecasting methods,
     * evaluates their accuracy using MAPE, and selects the optimal algorithm.
     * Demonstrates Core Polymorphism:
     * for (Forecaster f : forecasters) { f.forecast(days); }
     *
     * @param productId target product ID
     * @param days      forecast horizon in days (e.g. 7)
     * @return comparison result map
     */
    public Map<String, Object> compare(int productId, int days) {
        Product product = productRepo.findById(productId);
        if (product == null) {
            throw new ProductNotFoundException("Produk ID " + productId + " tidak ditemukan");
        }

        List<DailySales> history = trxRepo.findDailySales(productId);
        if (history == null || history.size() < 3) {
            int currentDays = history != null ? history.size() : 0;
            throw new InsufficientDataException(String.format(
                    "Data penjualan produk '%s' belum mencukupi untuk peramalan (Tersedia: %d hari, Dibutuhkan minimal: 3 hari)",
                    product.getName(), currentDays));
        }

        // Instantiate both forecasters
        List<Forecaster> forecasters = new ArrayList<>();
        forecasters.add(new MovingAverageForecaster(history, 3));
        forecasters.add(new LinearTrendForecaster(history));

        List<Map<String, Object>> methodResults = new ArrayList<>();
        Forecaster bestForecaster = null;
        double lowestMape = Double.MAX_VALUE;

        // POLYMORPHISM LOOP: Iterating over abstract base class Forecaster
        for (Forecaster f : forecasters) {
            List<ForecastResult> predictions = f.forecast(days);
            double mape = f.evaluateHistoricalMape();
            int restockRecommendation = f.recommendRestock(product.getStock(), days);

            double sumPredicted = 0.0;
            for (ForecastResult fr : predictions) {
                sumPredicted += fr.getPredictedQty();
            }

            Map<String, Object> methodData = new HashMap<>();
            methodData.put("methodName", f.getMethodName());
            methodData.put("mape", mape);
            methodData.put("predictedTotal", Math.round(sumPredicted));
            methodData.put("restockRecommendation", restockRecommendation);
            methodData.put("forecasts", predictions);

            methodResults.add(methodData);

            // Selection of best method based on lowest MAPE
            if (mape < lowestMape) {
                lowestMape = mape;
                bestForecaster = f;
            }
        }

        if (bestForecaster == null) {
            bestForecaster = forecasters.get(0);
        }

        // Calculation of restock components for the recommended method (Bab 2.7 d)
        List<ForecastResult> bestPredictions = bestForecaster.forecast(days);
        double bestTotalForecast = 0.0;
        for (ForecastResult r : bestPredictions) {
            bestTotalForecast += r.getPredictedQty();
        }
        double safetyStock = 0.20 * bestTotalForecast;
        int finalRestockQty = bestForecaster.recommendRestock(product.getStock(), days);

        Map<String, Object> response = new HashMap<>();
        response.put("product", Map.of(
                "id", product.getId(),
                "name", product.getName(),
                "category", product.getCategory(),
                "currentStock", product.getStock(),
                "minStock", product.getMinStock()
        ));
        response.put("history", history);
        response.put("horizonDays", days);
        response.put("methods", methodResults);
        response.put("bestMethod", bestForecaster.getMethodName());
        response.put("bestMape", lowestMape);
        response.put("recommendation", Map.of(
                "predictedTotal7Days", Math.round(bestTotalForecast),
                "safetyStock", Math.round(safetyStock),
                "currentStock", product.getStock(),
                "restockQty", finalRestockQty,
                "formula", "restock = max(0, totalPrediksi + safetyStock(20%) - currentStock)"
        ));

        return response;
    }

    /**
     * Obtains the superior forecaster for a product based on historical MAPE.
     */
    public Forecaster getBestForecaster(int productId) {
        List<DailySales> history = trxRepo.findDailySales(productId);
        if (history == null || history.size() < 3) {
            throw new InsufficientDataException("Data historis tidak mencukupi");
        }

        Forecaster ma = new MovingAverageForecaster(history, 3);
        Forecaster lr = new LinearTrendForecaster(history);

        return (ma.evaluateHistoricalMape() <= lr.evaluateHistoricalMape()) ? ma : lr;
    }

    /**
     * Directly calculates recommended restock quantity for a product.
     */
    public int getRecommendation(int productId) {
        Product product = productRepo.findById(productId);
        if (product == null) {
            throw new ProductNotFoundException("Produk ID " + productId + " tidak ditemukan");
        }
        Forecaster best = getBestForecaster(productId);
        return best.recommendRestock(product.getStock(), 7);
    }
}

