package com.predistock.forecast;

import com.predistock.exception.InsufficientDataException;
import com.predistock.model.DailySales;
import com.predistock.util.MathUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Concrete Forecaster implementing manual Ordinary Least Squares (OLS) Linear Regression Trend.
 * STRICTLY NO machine learning or external math libraries used.
 * Demonstrates:
 * - Inheritance (extends Forecaster)
 * - Polymorphism (concrete implementation of forecast() and getMethodName())
 * - Manual statistical slope and intercept calculation (OLS)
 */
public class LinearTrendForecaster extends Forecaster {
    private double slope;     // b
    private double intercept; // a

    public LinearTrendForecaster(List<DailySales> history) {
        super(history);
        if (history.size() < 2) {
            throw new InsufficientDataException("Data historis minimal 2 hari untuk regresi linear tren");
        }
        fit();
    }

    public double getSlope() {
        return slope;
    }

    public double getIntercept() {
        return intercept;
    }

    @Override
    public String getMethodName() {
        return "Regresi Linear Tren";
    }

    /**
     * Bab 2.7 (b) Manual Ordinary Least Squares (OLS) fitting:
     * x = day index (1, 2, ..., n)
     * y = qty sold
     *
     * b = (n * \Sigma xy - \Sigma x * \Sigma y) / (n * \Sigma x^2 - (\Sigma x)^2)
     * a = \bar{y} - b * \bar{x}
     */
    private void fit() {
        int n = history.size();
        List<Double> x = new ArrayList<>(n);
        List<Double> y = new ArrayList<>(n);

        for (int i = 0; i < n; i++) {
            x.add((double) (i + 1)); // x = 1, 2, ..., n
            y.add((double) history.get(i).getQtySold());
        }

        double sumX = MathUtil.sum(x);
        double sumY = MathUtil.sum(y);
        double sumXY = MathUtil.sumProduct(x, y);
        double sumXSquare = MathUtil.sumSquare(x);

        double meanX = sumX / n;
        double meanY = sumY / n;

        double denominator = (n * sumXSquare) - (sumX * sumX);
        if (Math.abs(denominator) < 1e-9) {
            // Degenerate case (constant x values)
            this.slope = 0.0;
            this.intercept = meanY;
        } else {
            double numerator = (n * sumXY) - (sumX * sumY);
            this.slope = numerator / denominator;
            this.intercept = meanY - (this.slope * meanX);
        }
    }

    /**
     * Forecasts sales for future days using the fitted linear equation:
     * \hat{y}(x) = a + b * x
     *
     * @param days horizon count
     * @return forecasted daily sales
     */
    @Override
    public List<ForecastResult> forecast(int days) {
        if (days <= 0) {
            throw new IllegalArgumentException("Jumlah hari ramalan (days) harus lebih besar dari 0");
        }

        List<ForecastResult> results = new ArrayList<>();
        int n = history.size();
        LocalDate lastDate = history.get(n - 1).getDate();

        for (int i = 1; i <= days; i++) {
            int x = n + i;
            double predicted = intercept + (slope * x);
            predicted = Math.max(0.0, predicted); // Sales cannot be negative
            predicted = MathUtil.round(predicted, 2);

            LocalDate targetDate = lastDate.plusDays(i);
            results.add(new ForecastResult(targetDate, predicted));
        }

        return results;
    }

    /**
     * In-sample accuracy evaluation on historical training points.
     */
    @Override
    public double evaluateHistoricalMape() {
        int n = history.size();
        List<Double> actuals = new ArrayList<>(n);
        List<Double> predicteds = new ArrayList<>(n);

        for (int i = 0; i < n; i++) {
            int x = i + 1;
            double pred = Math.max(0.0, intercept + (slope * x));
            actuals.add((double) history.get(i).getQtySold());
            predicteds.add(pred);
        }

        return calculateMape(actuals, predicteds);
    }
}

