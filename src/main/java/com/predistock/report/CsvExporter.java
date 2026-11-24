package com.predistock.report;

import com.predistock.model.SalesSummary;
import com.predistock.util.DateUtil;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

/**
 * Concrete exporter formatting SalesSummary data as Comma-Separated Values (CSV).
 * Demonstrates:
 * - Interface Implementation
 * - Polymorphism (Executes CSV-specific formatting rules)
 */
public class CsvExporter implements Exportable {

    @Override
    public String getFormat() {
        return "CSV";
    }

    @Override
    public String exportToString(SalesSummary summary) {
        if (summary == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Laporan Penjualan PrediStock (CSV)\n");
        sb.append("Periode Mulai,").append(DateUtil.formatDate(summary.getPeriodStart())).append("\n");
        sb.append("Periode Selesai,").append(DateUtil.formatDate(summary.getPeriodEnd())).append("\n");
        sb.append(String.format("Total Pendapatan (Rp),%.0f\n", summary.getTotalRevenue()));
        sb.append("Total Transaksi,").append(summary.getTotalTransactions()).append("\n\n");

        sb.append("Nama Produk,Jumlah Terjual (Unit)\n");
        for (Map.Entry<String, Integer> entry : summary.getQtyPerProduct().entrySet()) {
            sb.append("\"").append(entry.getKey()).append("\",").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }

    @Override
    public void export(SalesSummary summary, String path) {
        String content = exportToString(summary);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            writer.write(content);
        } catch (IOException e) {
            throw new RuntimeException("Gagal mengekspor laporan CSV ke: " + path, e);
        }
    }
}

