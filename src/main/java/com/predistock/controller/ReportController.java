package com.predistock.controller;

import com.predistock.model.SalesSummary;
import com.predistock.report.CsvExporter;
import com.predistock.report.Exportable;
import com.predistock.report.TextReportExporter;
import com.predistock.service.ReportService;
import com.predistock.util.DateUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Sales Reports, Visual Chart Data, and Export Operations.
 * Strictly implements the REST endpoints outlined in Section 2.10 of the proposal.
 */
@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * GET /api/reports/daily?date=YYYY-MM-DD
     * Feature E-01: Retrieves daily financial and sales breakdown.
     */
    @GetMapping("/daily")
    public ResponseEntity<Map<String, Object>> handleDaily(
            @RequestParam(name = "date", required = false) String dateStr) {

        LocalDate date = (dateStr != null && !dateStr.isEmpty())
                ? DateUtil.parseIsoDate(dateStr)
                : LocalDate.now();

        SalesSummary summary = reportService.getDailyReport(date);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", convertSummaryToMap(summary));
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/reports/monthly?month=YYYY-MM
     * Feature E-02: Retrieves monthly financial summary.
     */
    @GetMapping("/monthly")
    public ResponseEntity<Map<String, Object>> handleMonthly(
            @RequestParam(name = "month", required = false) String monthStr) {

        YearMonth ym = (monthStr != null && !monthStr.isEmpty())
                ? DateUtil.parseYearMonth(monthStr)
                : YearMonth.now();

        SalesSummary summary = reportService.getMonthlyReport(ym);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", convertSummaryToMap(summary));
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/reports/chart?from=YYYY-MM-DD&to=YYYY-MM-DD
     * Feature E-04: Returns time-series dataset formatted for Chart.js line charts.
     */
    @GetMapping("/chart")
    public ResponseEntity<Map<String, Object>> handleChart(
            @RequestParam(name = "from", required = false) String fromStr,
            @RequestParam(name = "to", required = false) String toStr) {

        LocalDate from = (fromStr != null && !fromStr.isEmpty())
                ? DateUtil.parseIsoDate(fromStr)
                : LocalDate.now().minusDays(14);

        LocalDate to = (toStr != null && !toStr.isEmpty())
                ? DateUtil.parseIsoDate(toStr)
                : LocalDate.now();

        Map<String, Object> chartData = reportService.getChartData(from, to);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", chartData);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/reports/top-products?n=5&from=YYYY-MM-DD&to=YYYY-MM-DD
     * Feature E-03: Retrieves top N best-selling products sorted by sales volume.
     */
    @GetMapping("/top-products")
    public ResponseEntity<Map<String, Object>> handleTopProducts(
            @RequestParam(name = "n", defaultValue = "5") int n,
            @RequestParam(name = "from", required = false) String fromStr,
            @RequestParam(name = "to", required = false) String toStr) {

        LocalDate from = (fromStr != null && !fromStr.isEmpty())
                ? DateUtil.parseIsoDate(fromStr)
                : LocalDate.now().minusDays(30);

        LocalDate to = (toStr != null && !toStr.isEmpty())
                ? DateUtil.parseIsoDate(toStr)
                : LocalDate.now();

        List<String> topList = reportService.getTopProducts(n, from, to);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("periodFrom", from.toString());
        response.put("periodTo", to.toString());
        response.put("topCount", n);
        response.put("topProducts", topList);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/reports/export?format=csv&from=YYYY-MM-DD&to=YYYY-MM-DD
     * Feature E-05: Polymorphic export to CSV or Text file.
     * Demonstrates dynamic polymorphism on the Exportable interface.
     */
    @GetMapping("/export")
    public ResponseEntity<String> handleExport(
            @RequestParam(name = "format", defaultValue = "csv") String format,
            @RequestParam(name = "from", required = false) String fromStr,
            @RequestParam(name = "to", required = false) String toStr) {

        LocalDate from = (fromStr != null && !fromStr.isEmpty())
                ? DateUtil.parseIsoDate(fromStr)
                : LocalDate.now().minusDays(30);

        LocalDate to = (toStr != null && !toStr.isEmpty())
                ? DateUtil.parseIsoDate(toStr)
                : LocalDate.now();

        SalesSummary summary = reportService.getRangeReport(from, to);

        // Polymorphic strategy selection
        Exportable exporter;
        String contentType;
        String filename;

        if ("txt".equalsIgnoreCase(format) || "text".equalsIgnoreCase(format)) {
            exporter = new TextReportExporter();
            contentType = MediaType.TEXT_PLAIN_VALUE;
            filename = "laporan-penjualan.txt";
        } else {
            exporter = new CsvExporter();
            contentType = "text/csv";
            filename = "laporan-penjualan.csv";
        }

        // Polymorphic invocation: exporter.exportToString(summary)
        String reportOutput = reportService.exportReportToString(summary, exporter);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_TYPE, contentType + "; charset=UTF-8")
                .body(reportOutput);
    }

    private Map<String, Object> convertSummaryToMap(SalesSummary s) {
        Map<String, Object> map = new HashMap<>();
        map.put("periodStart", s.getPeriodStart().toString());
        map.put("periodEnd", s.getPeriodEnd().toString());
        map.put("totalRevenue", s.getTotalRevenue());
        map.put("totalTransactions", s.getTotalTransactions());
        map.put("qtyPerProduct", s.getQtyPerProduct());
        map.put("topProducts", s.getTopProducts(5));
        return map;
    }
}

