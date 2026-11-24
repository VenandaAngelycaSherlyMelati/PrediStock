package com.predistock.util;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Utility class for calendar calculations, formatting, and date manipulation.
 * Demonstrates:
 * - Utility Class Pattern (Private constructor, pure static methods)
 * - Safe handling of temporal operations without third-party libraries
 */
public final class DateUtil {

    private static final DateTimeFormatter STANDARD_DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter ISO_DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private DateUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Calculates the number of days elapsed between two dates.
     *
     * @param a start date
     * @param b end date
     * @return days difference
     */
    public static int daysBetween(LocalDate a, LocalDate b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("Dates cannot be null");
        }
        return (int) ChronoUnit.DAYS.between(a, b);
    }

    /**
     * Returns the first calendar date of a given YearMonth.
     *
     * @param ym target YearMonth
     * @return LocalDate of first day of the month
     */
    public static LocalDate startOfMonth(YearMonth ym) {
        if (ym == null) {
            ym = YearMonth.now();
        }
        return ym.atDay(1);
    }

    /**
     * Returns the last calendar date of a given YearMonth.
     *
     * @param ym target YearMonth
     * @return LocalDate of last day of the month
     */
    public static LocalDate endOfMonth(YearMonth ym) {
        if (ym == null) {
            ym = YearMonth.now();
        }
        return ym.atEndOfMonth();
    }

    /**
     * Formats a LocalDate into standard Indonesian readable format (dd-MM-yyyy).
     *
     * @param d date
     * @return formatted date string
     */
    public static String formatDate(LocalDate d) {
        if (d == null) {
            return "-";
        }
        return d.format(STANDARD_DATE_FORMAT);
    }

    /**
     * Parses an ISO string (YYYY-MM-DD) into a LocalDate.
     *
     * @param str ISO date string
     * @return parsed LocalDate
     */
    public static LocalDate parseIsoDate(String str) {
        if (str == null || str.trim().isEmpty()) {
            return LocalDate.now();
        }
        return LocalDate.parse(str.trim());
    }

    /**
     * Parses a YearMonth string (YYYY-MM).
     *
     * @param str YearMonth string
     * @return parsed YearMonth
     */
    public static YearMonth parseYearMonth(String str) {
        if (str == null || str.trim().isEmpty()) {
            return YearMonth.now();
        }
        return YearMonth.parse(str.trim());
    }
}

