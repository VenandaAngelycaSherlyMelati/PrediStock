package com.predistock.controller;

import com.predistock.exception.InsufficientDataException;
import com.predistock.exception.ProductNotFoundException;
import com.predistock.service.ForecastService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for Sales Forecasting and Inventory Restock Recommendations.
 * Strictly implements the endpoint GET /api/forecast/{productId}?days=7 (Section 2.10).
 */
@RestController
@RequestMapping("/api/forecast")
@CrossOrigin(origins = "*")
public class ForecastController {

    private final ForecastService forecastService;

    public ForecastController(ForecastService forecastService) {
        this.forecastService = forecastService;
    }

    /**
     * GET /api/forecast/{productId}?days=7
     * Compares Moving Average & Linear Regression, calculates MAPE, and provides restock guidance.
     */
    @GetMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> handleForecast(
            @PathVariable("productId") int productId,
            @RequestParam(name = "days", defaultValue = "7") int days) {

        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> comparison = forecastService.compare(productId, days);
            response.put("success", true);
            response.put("data", comparison);
            return ResponseEntity.ok(response);

        } catch (InsufficientDataException e) {
            response.put("success", false);
            response.put("error", "INSUFFICIENT_DATA");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (ProductNotFoundException e) {
            response.put("success", false);
            response.put("error", "PRODUCT_NOT_FOUND");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Terjadi kesalahan peramalan: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}

