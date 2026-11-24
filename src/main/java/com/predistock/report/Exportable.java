package com.predistock.report;

import com.predistock.model.SalesSummary;

/**
 * Interface defining contract for exporting sales summaries into different formats.
 * Demonstrates:
 * - Interface Contract
 * - Polymorphism (Exportable reference can dynamically export to CSV or plain Text)
 */
public interface Exportable {

    /**
     * Exports sales summary report and writes output directly to a file on disk.
     *
     * @param summary the sales summary model to export
     * @param path    the destination file path
     */
    void export(SalesSummary summary, String path);

    /**
     * Exports sales summary report and returns content as a formatted String.
     *
     * @param summary the sales summary model
     * @return formatted report string
     */
    String exportToString(SalesSummary summary);

    /**
     * Obtains the target format identifier (e.g. "CSV", "TXT").
     *
     * @return format extension string
     */
    String getFormat();
}

