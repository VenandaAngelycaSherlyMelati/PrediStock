package com.predistock.service;

import com.predistock.model.SalesSummary;
import com.predistock.report.Exportable;
import com.predistock.report.ReportGenerator;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service orchestrating financial reporting, analytics aggregation, and report exporting.
 * Strictly adheres to Clean Architecture: Controller -> Service -> ReportGenerator -> Repository.
 */
@Service
public class ReportService {

    private final ReportGenerator generator;

    public ReportService(ReportGenerator generator) {
        this.generator = generator;
    }

    /**
     * E-01: Obtains daily sales summary.
     */
    public SalesSummary getDailyReport(LocalDate d) {
        return generator.generateDaily(d);
    }

    /**
     * E-02: Obtains monthly sales summary.
     */
    public SalesSummary getMonthlyReport(YearMonth ym) {
        return generator.generateMonthly(ym);
    }

    /**
     * Obtains sales summary for an arbitrary custom date range.
     */
    public SalesSummary getRangeReport(LocalDate from, LocalDate to) {
        return generator.generateForDateRange(from, to);
    }

    /**
     * E-04: Prepares data payload specifically structured for frontend Chart.js components.
     * Contains separate 'labels' (dates) and 'datasets' (revenue values).
     */
    public Map<String, Object> getChartData(LocalDate from, LocalDate to) {
        Map<String, Double> rawDailyMap = generator.getChartData(from, to);

        List<String> labels = new ArrayList<>(rawDailyMap.keySet());
        List<Double> dataValues = new ArrayList<>(rawDailyMap.values());

        double totalPeriodRevenue = 0.0;
        for (Double val : dataValues) {
            totalPeriodRevenue += val;
        }

        Map<String, Object> chartPayload = new HashMap<>();
        chartPayload.put("labels", labels);
        chartPayload.put("data", dataValues);
        chartPayload.put("totalRevenue", totalPeriodRevenue);
        chartPayload.put("periodFrom", from.toString());
        chartPayload.put("periodTo", to.toString());

        return chartPayload;
    }

    /**
     * E-05: Polymorphic export to file on disk.
     * Demonstrates dynamic polymorphism: e.export() executes either CsvExporter or TextReportExporter.
     *
     * @param s    SalesSummary to export
     * @param e    Polymorphic Exportable strategy
     * @param path destination file path
     */
    public void exportReport(SalesSummary s, Exportable e, String path) {
        if (s == null || e == null) {
            throw new IllegalArgumentException("Summary and Exporter cannot be null");
        }
        e.export(s, path);
    }

    /**
     * E-05: Polymorphic export to formatted string.
     *
     * @param s SalesSummary to export
     * @param e Polymorphic Exportable strategy
     * @return formatted report string
     */
    public String exportReportToString(SalesSummary s, Exportable e) {
        if (s == null || e == null) {
            throw new IllegalArgumentException("Summary and Exporter cannot be null");
        }
        return e.exportToString(s);
    }

    /**
     * E-03: Obtains the top N best-selling products across a given date range.
     */
    public List<String> getTopProducts(int n, LocalDate from, LocalDate to) {
        SalesSummary summary = generator.generateForDateRange(from, to);
        return summary.getTopProducts(n);
    }
}

