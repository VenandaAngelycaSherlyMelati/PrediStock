package com.predistock.forecast;

import com.predistock.exception.InsufficientDataException;
import com.predistock.model.DailySales;
import com.predistock.util.MathUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Concrete Forecaster implementing manual N-day rolling Moving Average.
 * Demonstrates:
 * - Inheritance (extends Forecaster)
 * - Polymorphism (concrete implementation of forecast() and getMethodName())
 * - Manual Rolling Forecast logic without ML libraries
 */
public class MovingAverageForecaster extends Forecaster {
    private final int window;

    public MovingAverageForecaster(List<DailySales> history, int window) {
        super(history);
        if (window <= 0) {
            throw new IllegalArgumentException("Window size harus lebih besar dari 0");
        }
        if (history.size() < window) {
            throw new InsufficientDataException(String.format(
                    "Data historis (%d hari) tidak mencukupi untuk Moving Average dengan window %d",
                    history.size(), window));
        }
        this.window = window;
    }

    public MovingAverageForecaster(List<DailySales> history) {
        this(history, 3); // default window = 3 as specified in proposal Bab 2.7
    }

    public int getWindow() {
        return window;
    }

    @Override
    public String getMethodName() {
        return "Moving Average (Window " + window + ")";
    }

    /**
     * Bab 2.7 (a) Manual Rolling Moving Average implementation:
     * \hat{y}(t+1) = (y(t) + y(t-1) + ... + y(t-n+1)) / n
     * For future multi-day horizon, newly predicted values are rolled into the sliding window.
     */
    @Override
    public List<ForecastResult> forecast(int days) {
        if (days <= 0) {
            throw new IllegalArgumentException("Jumlah hari ramalan (days) harus lebih besar dari 0");
        }

        List<ForecastResult> results = new ArrayList<>();
        // Extract historical values
        List<Double> series = new ArrayList<>();
        for (DailySales ds : history) {
            series.add((double) ds.getQtySold());
        }

        LocalDate lastDate = history.get(history.size() - 1).getDate();

        for (int i = 0; i < days; i++) {
            // Take the last 'window' elements from series
            int size = series.size();
            double sumWindow = 0.0;
            for (int w = 0; w < window; w++) {
                sumWindow += series.get(size - 1 - w);
            }
            double nextVal = sumWindow / window;
            nextVal = MathUtil.round(nextVal, 2);

            LocalDate forecastDate = lastDate.plusDays(i + 1);
            results.add(new ForecastResult(forecastDate, nextVal));

            // Rolling forecast: append predicted value back into series for next day's calculation
            series.add(nextVal);
        }

        return results;
    }

    /**
     * Evaluates in-sample historical accuracy on available data points.
     */
    @Override
    public double evaluateHistoricalMape() {
        if (history.size() <= window) {
            return 0.0;
        }

        List<Double> actuals = new ArrayList<>();
        List<Double> predicteds = new ArrayList<>();

        for (int i = window; i < history.size(); i++) {
            double sumWindow = 0.0;
            for (int w = 1; w <= window; w++) {
                sumWindow += history.get(i - w).getQtySold();
            }
            double pred = sumWindow / window;

            actuals.add((double) history.get(i).getQtySold());
            predicteds.add(pred);
        }

        return calculateMape(actuals, predicteds);
    }
}

