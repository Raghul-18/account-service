package com.bank.account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main application class for Account Service
 *
 * Features:
 * - Account creation and management
 * - Balance management with constraints
 * - Inter-service communication with Customer and KYC services
 * - Kafka integration for automated account creation
 * - JWT-based authentication and authorization
 */
@SpringBootApplication
@EnableTransactionManagement
public class AccountServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountServiceApplication.class, args);
        System.out.println("🏧 Account Service started successfully on port 8083");
        System.out.println("📊 Swagger UI: http://localhost:8083/swagger-ui.html");
        System.out.println("🔍 API Docs: http://localhost:8083/v3/api-docs");
    }
}