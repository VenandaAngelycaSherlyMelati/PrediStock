package com.predistock;

import com.predistock.repository.DatabaseConnection;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Main application bootstrap for PrediStock backend.
 */
@SpringBootApplication
public class PrediStockApplication {

    public static void main(String[] args) {
        // Initialize SQLite schema and default data before or during startup
        DatabaseConnection.initTables();
        SpringApplication.run(PrediStockApplication.class, args);
    }

    @Bean
    public CommandLineRunner initDatabase() {
        return args -> {
            // Re-affirm schema and table initialization & user dataset
            DatabaseConnection.initTables();
            System.out.println("==================================================");
            System.out.println("   PrediStock Backend is Running on port 8080!    ");
            System.out.println("   Database: SQLite (Raw JDBC mode)              ");
            System.out.println("   Location: " + DatabaseConnection.getDatabasePath());
            System.out.println("   Store: Toko Zaini Sejahtera                    ");
            System.out.println("   Owner:     owner  / admin123 (Tubagus Zaini)   ");
            System.out.println("   Cashier 1: kasir1 / kasir123 (Ruqayyah)        ");
            System.out.println("   Cashier 2: kasir2 / kasir123 (Ida)             ");
            System.out.println("==================================================");
        };
    }
}

