package com.predistock.report;

import com.predistock.model.SalesSummary;
import com.predistock.util.DateUtil;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Concrete exporter formatting SalesSummary data as human-readable plain text reports.
 * Demonstrates:
 * - Interface Implementation
 * - Polymorphism (Executes Text-specific layout rules)
 */
public class TextReportExporter implements Exportable {

    @Override
    public String getFormat() {
        return "TXT";
    }

    @Override
    public String exportToString(SalesSummary summary) {
        if (summary == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("====================================================\n");
        sb.append("           LAPORAN PENJUALAN PREDISTOCK             \n");
        sb.append("====================================================\n");
        sb.append("Periode           : ").append(DateUtil.formatDate(summary.getPeriodStart()))
          .append(" s/d ").append(DateUtil.formatDate(summary.getPeriodEnd())).append("\n");
        sb.append(String.format("Total Pendapatan  : Rp %,.0f\n", summary.getTotalRevenue()));
        sb.append("Total Transaksi   : ").append(summary.getTotalTransactions()).append(" transaksi\n");
        sb.append("----------------------------------------------------\n");
        sb.append("RINCIAN PENJUALAN PRODUK:\n");
        sb.append(String.format("%-32s %15s\n", "Nama Produk", "Terjual (Unit)"));
        sb.append("----------------------------------------------------\n");

        if (summary.getQtyPerProduct().isEmpty()) {
            sb.append("  (Belum ada data penjualan pada periode ini)\n");
        } else {
            for (Map.Entry<String, Integer> entry : summary.getQtyPerProduct().entrySet()) {
                String pName = entry.getKey();
                if (pName.length() > 30) {
                    pName = pName.substring(0, 27) + "...";
                }
                sb.append(String.format("%-32s %15d\n", pName, entry.getValue()));
            }
        }

        sb.append("----------------------------------------------------\n");
        sb.append("PRODUK TERLARIS (TOP 5):\n");
        List<String> top5 = summary.getTopProducts(5);
        if (top5.isEmpty()) {
            sb.append("  -\n");
        } else {
            for (int i = 0; i < top5.size(); i++) {
                sb.append(String.format("  %d. %s\n", (i + 1), top5.get(i)));
            }
        }
        sb.append("====================================================\n");
        sb.append("           Dicetak otomatis oleh PrediStock         \n");
        sb.append("====================================================\n");

        return sb.toString();
    }

    @Override
    public void export(SalesSummary summary, String path) {
        String content = exportToString(summary);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            writer.write(content);
        } catch (IOException e) {
            throw new RuntimeException("Gagal mengekspor laporan teks ke: " + path, e);
        }
    }
}

