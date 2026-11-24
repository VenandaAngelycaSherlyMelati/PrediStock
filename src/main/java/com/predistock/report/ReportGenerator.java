package com.predistock.report;

import com.predistock.model.SalesSummary;
import com.predistock.model.Transaction;
import com.predistock.model.TransactionItem;
import com.predistock.repository.TransactionRepository;
import com.predistock.util.DateUtil;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Component responsible for querying transactional data and synthesizing SalesSummary metrics.
 * Demonstrates:
 * - Data Aggregation using Map and Collection
 * - Clean Separation of Concerns between data generation and export presentation
 */
@Component
public class ReportGenerator {

    private final TransactionRepository trxRepo;

    public ReportGenerator(TransactionRepository trxRepo) {
        this.trxRepo = trxRepo;
    }

    /**
     * Feature E-01: Generates a daily summary of sales revenue and quantities for a single calendar date.
     *
     * @param date target date
     * @return SalesSummary object
     */
    public SalesSummary generateDaily(LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        return generateForDateRange(date, date);
    }

    /**
     * Feature E-02: Generates a monthly summary of sales for a specific YearMonth.
     *
     * @param ym target YearMonth
     * @return SalesSummary object
     */
    public SalesSummary generateMonthly(YearMonth ym) {
        if (ym == null) {
            ym = YearMonth.now();
        }
        LocalDate start = DateUtil.startOfMonth(ym);
        LocalDate end = DateUtil.endOfMonth(ym);
        return generateForDateRange(start, end);
    }

    /**
     * Generates a SalesSummary for an arbitrary date window [from, to].
     *
     * @param from start date
     * @param to   end date
     * @return SalesSummary object
     */
    public SalesSummary generateForDateRange(LocalDate from, LocalDate to) {
        List<Transaction> transactions = trxRepo.findByDateRange(from, to);

        double totalRevenue = 0.0;
        int totalTransactions = transactions.size();
        Map<String, Integer> qtyPerProduct = new HashMap<>();

        for (Transaction trx : transactions) {
            totalRevenue += trx.calculateTotal();

            for (TransactionItem item : trx.getItems()) {
                String productName = item.getProduct().getName();
                qtyPerProduct.put(
                        productName,
                        qtyPerProduct.getOrDefault(productName, 0) + item.getQty()
                );
            }
        }

        return new SalesSummary(from, to, totalRevenue, totalTransactions, qtyPerProduct);
    }

    /**
     * Feature E-04: Prepares chronological time-series data for frontend visual charting (Chart.js).
     *
     * @param from start date
     * @param to   end date
     * @return chronologically sorted map of (Date String -> Total Revenue)
     */
    public Map<String, Double> getChartData(LocalDate from, LocalDate to) {
        if (from == null) {
            from = LocalDate.now().minusDays(14);
        }
        if (to == null) {
            to = LocalDate.now();
        }

        List<Transaction> transactions = trxRepo.findByDateRange(from, to);

        // Pre-populate all days in range with 0.0 to ensure continuous Chart.js line
        Map<String, Double> chartMap = new TreeMap<>();
        LocalDate cur = from;
        while (!cur.isAfter(to)) {
            chartMap.put(cur.toString(), 0.0);
            cur = cur.plusDays(1);
        }

        for (Transaction trx : transactions) {
            String dayStr = trx.getDate().toLocalDate().toString();
            chartMap.put(
                    dayStr,
                    chartMap.getOrDefault(dayStr, 0.0) + trx.calculateTotal()
            );
        }

        return chartMap;
    }
}

