package com.bank.account.util;

import java.math.BigDecimal;

/**
 * Application constants for Account Service
 */
public final class Constants {

    // Prevent instantiation
    private Constants() {}

    // ===== ACCOUNT CONSTANTS =====

    public static final class Account {
        // Account number format components
        public static final String DEFAULT_PREFIX = "BANK";
        public static final int ACCOUNT_NUMBER_MIN_LENGTH = 10;
        public static final int ACCOUNT_NUMBER_MAX_LENGTH = 20;
        public static final int SEQUENCE_LENGTH = 3;

        // Account type codes
        public static final String SAVINGS_CODE = "SAV";
        public static final String CURRENT_CODE = "CUR";

        // Default minimum balances
        public static final BigDecimal SAVINGS_MIN_BALANCE = new BigDecimal("1000.00");
        public static final BigDecimal CURRENT_MIN_BALANCE = new BigDecimal("5000.00");

        // Maximum accounts per customer per type
        public static final int MAX_ACCOUNTS_PER_TYPE = 1;

        // Account number generation retry attempts
        public static final int MAX_GENERATION_ATTEMPTS = 100;
    }

    // ===== BALANCE CONSTANTS =====

    public static final class Balance {
        public static final BigDecimal ZERO = BigDecimal.ZERO;
        public static final BigDecimal MIN_TRANSACTION_AMOUNT = new BigDecimal("0.01");
        public static final BigDecimal MAX_BALANCE_LIMIT = new BigDecimal("99999999999.99");

        // Balance warning thresholds
        public static final BigDecimal LOW_BALANCE_THRESHOLD_MULTIPLIER = new BigDecimal("1.5");
        public static final BigDecimal CRITICAL_BALANCE_THRESHOLD_MULTIPLIER = new BigDecimal("1.1");
    }

    // ===== API CONSTANTS =====

    public static final class Api {
        // Base paths
        public static final String BASE_PATH = "/api/accounts";
        public static final String ADMIN_PATH = "/api/accounts/admin";
        public static final String INTERNAL_PATH = "/api/accounts/internal";

        // Common endpoints
        public static final String CREATE = "/create";
        public static final String GET_BY_ID = "/{accountId}";
        public static final String GET_BY_CUSTOMER = "/{customerId}";
        public static final String UPDATE_BALANCE = "/{accountId}/balance";
        public static final String UPDATE_STATUS = "/{accountId}/status";
        public static final String DELETE = "/{accountId}";

        // Admin endpoints
        public static final String ADMIN_ALL = "/all";
        public static final String ADMIN_STATS = "/stats";
        public static final String ADMIN_BY_STATUS = "/by-status/{status}";
        public static final String ADMIN_BY_TYPE = "/by-type/{type}";

        // Internal endpoints
        public static final String INTERNAL_CREATE_AFTER_KYC = "/create-after-kyc";
        public static final String INTERNAL_CUSTOMER_EXISTS = "/customer/{customerId}/exists";
        public static final String INTERNAL_VALIDATE_ACCOUNT = "/validate/{accountNumber}";
    }

    // ===== ERROR CODES =====

    public static final class ErrorCodes {
        public static final String ACCOUNT_NOT_FOUND = "ACCOUNT_NOT_FOUND";
        public static final String CUSTOMER_NOT_FOUND = "CUSTOMER_NOT_FOUND";
        public static final String DUPLICATE_ACCOUNT = "DUPLICATE_ACCOUNT";
        public static final String INSUFFICIENT_BALANCE = "INSUFFICIENT_BALANCE";
        public static final String INVALID_BALANCE_OPERATION = "INVALID_BALANCE_OPERATION";
        public static final String INVALID_STATUS_TRANSITION = "INVALID_STATUS_TRANSITION";
        public static final String UNAUTHORIZED_ACCESS = "UNAUTHORIZED_ACCESS";
        public static final String VALIDATION_FAILED = "VALIDATION_FAILED";
        public static final String ACCOUNT_GENERATION_FAILED = "ACCOUNT_GENERATION_FAILED";
        public static final String INVALID_ACCOUNT_TYPE = "INVALID_ACCOUNT_TYPE";
        public static final String ACCOUNT_CLOSED = "ACCOUNT_CLOSED";
        public static final String ACCOUNT_INACTIVE = "ACCOUNT_INACTIVE";
    }

    // ===== SUCCESS MESSAGES =====

    public static final class Messages {
        // Account operations
        public static final String ACCOUNT_CREATED = "Account created successfully";
        public static final String ACCOUNT_UPDATED = "Account updated successfully";
        public static final String ACCOUNT_DELETED = "Account deleted successfully";
        public static final String BALANCE_UPDATED = "Balance updated successfully";
        public static final String STATUS_UPDATED = "Account status updated successfully";

        // Retrieval messages
        public static final String ACCOUNT_RETRIEVED = "Account details retrieved successfully";
        public static final String ACCOUNTS_RETRIEVED = "Accounts retrieved successfully";
        public static final String BALANCE_RETRIEVED = "Balance information retrieved successfully";
        public static final String STATISTICS_RETRIEVED = "Account statistics retrieved successfully";

        // Validation messages
        public static final String ACCOUNT_VALIDATED = "Account validation successful";
        public static final String ACCOUNT_EXISTS = "Account exists";
        public static final String ACCOUNT_AVAILABLE = "Account number available";
    }

    // ===== PAGINATION CONSTANTS =====

    public static final class Pagination {
        public static final int DEFAULT_PAGE_SIZE = 20;
        public static final int MAX_PAGE_SIZE = 100;
        public static final int DEFAULT_PAGE_NUMBER = 0;
        public static final String DEFAULT_SORT_BY = "createdAt";
        public static final String DEFAULT_SORT_DIRECTION = "DESC";
    }

    // ===== DATE/TIME CONSTANTS =====

    public static final class DateTime {
        public static final String DATE_FORMAT = "yyyy-MM-dd";
        public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
        public static final String ISO_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

        // Dormant account threshold (days)
        public static final int DORMANT_ACCOUNT_DAYS = 365;

        // Recent activity periods (days)
        public static final int RECENT_ACTIVITY_DAYS = 30;
        public static final int WEEKLY_ACTIVITY_DAYS = 7;
    }

    // ===== VALIDATION CONSTANTS =====

    public static final class Validation {
        // Field length limits
        public static final int REMARKS_MAX_LENGTH = 500;
        public static final int REASON_MAX_LENGTH = 500;
        public static final int USERNAME_MAX_LENGTH = 100;
        public static final int REFERENCE_MAX_LENGTH = 50;

        // Decimal precision
        public static final int BALANCE_PRECISION = 15;
        public static final int BALANCE_SCALE = 2;

        // Regular expressions
        public static final String ACCOUNT_NUMBER_REGEX = "^BANK\\d+[A-Z]{3}\\d{3}$";
        public static final String PHONE_REGEX = "^\\d{10}$";
    }

    // ===== KAFKA CONSTANTS =====

    public static final class Kafka {
        // Topics
        public static final String CUSTOMER_VERIFIED_TOPIC = "customer-verified";
        public static final String ACCOUNT_CREATED_TOPIC = "account-created";
        public static final String BALANCE_UPDATED_TOPIC = "balance-updated";

        // Consumer groups
        public static final String ACCOUNT_SERVICE_GROUP = "account-service-group";

        // Event types
        public static final String CUSTOMER_VERIFIED_EVENT = "CustomerVerifiedEvent";
        public static final String ACCOUNT_CREATED_EVENT = "AccountCreatedEvent";
        public static final String BALANCE_UPDATED_EVENT = "BalanceUpdatedEvent";
    }

    // ===== ROLE CONSTANTS =====

    public static final class Roles {
        public static final String ADMIN = "ADMIN";
        public static final String CUSTOMER = "CUSTOMER";
        public static final String VERIFIER = "VERIFIER";
        public static final String SYSTEM = "SYSTEM";
    }

    // ===== HTTP HEADERS =====

    public static final class Headers {
        public static final String AUTHORIZATION = "Authorization";
        public static final String BEARER_PREFIX = "Bearer ";
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String APPLICATION_JSON = "application/json";
        public static final String USER_AGENT = "User-Agent";
        public static final String REQUEST_ID = "X-Request-ID";
    }

    // ===== SERVICE URLS =====

    public static final class ServiceUrls {
        public static final String CUSTOMER_SERVICE_BASE = "http://localhost:8081";
        public static final String KYC_SERVICE_BASE = "http://localhost:8084";
        public static final String GATEWAY_SERVICE_BASE = "http://localhost:8080";

        // Customer service endpoints
        public static final String CUSTOMER_BY_ID = "/api/customers/{customerId}";
        public static final String CUSTOMER_EXISTS = "/api/customers/{customerId}/exists";

        // KYC service endpoints
        public static final String KYC_STATUS = "/api/kyc/customer/{customerId}/status";
    }

    // ===== AUDIT CONSTANTS =====

    public static final class Audit {
        public static final String CREATED_BY_SYSTEM = "SYSTEM";
        public static final String CREATED_BY_KAFKA = "KAFKA_EVENT";
        public static final String CREATED_BY_ADMIN = "ADMIN";
        public static final String CREATED_BY_CUSTOMER = "CUSTOMER";

        // Operation types
        public static final String OPERATION_CREATE = "CREATE";
        public static final String OPERATION_UPDATE = "UPDATE";
        public static final String OPERATION_DELETE = "DELETE";
        public static final String OPERATION_BALANCE_UPDATE = "BALANCE_UPDATE";
        public static final String OPERATION_STATUS_UPDATE = "STATUS_UPDATE";
    }

    // ===== CACHE CONSTANTS =====

    public static final class Cache {
        public static final String ACCOUNT_CACHE = "accounts";
        public static final String CUSTOMER_CACHE = "customers";
        public static final String BALANCE_CACHE = "balances";

        // TTL in seconds
        public static final long DEFAULT_TTL = 300; // 5 minutes
        public static final long BALANCE_TTL = 60;   // 1 minute
        public static final long CUSTOMER_TTL = 600; // 10 minutes
    }
}