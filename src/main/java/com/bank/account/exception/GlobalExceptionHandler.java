package com.bank.account.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for Account Service
 * Provides consistent error response format across all endpoints
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle account not found exceptions
     */
    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleAccountNotFound(
            AccountNotFoundException ex, WebRequest request) {

        log.warn("❌ Account not found: {}", ex.getMessage());

        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "ACCOUNT_NOT_FOUND",
                ex.getMessage(),
                Map.of(
                        "accountId", ex.getAccountId(),
                        "accountNumber", ex.getAccountNumber()
                )
        );
    }

    /**
     * Handle insufficient balance exceptions
     */
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientBalance(
            InsufficientBalanceException ex, WebRequest request) {

        log.warn("❌ Insufficient balance: {}", ex.getMessage());

        return buildErrorResponse(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "INSUFFICIENT_BALANCE",
                ex.getMessage(),
                Map.of(
                        "accountNumber", ex.getAccountNumber(),
                        "currentBalance", ex.getCurrentBalance(),
                        "requestedAmount", ex.getRequestedAmount(),
                        "minimumBalance", ex.getMinimumBalance()
                )
        );
    }

    /**
     * Handle duplicate account exceptions
     */
    @ExceptionHandler(DuplicateAccountException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateAccount(
            DuplicateAccountException ex, WebRequest request) {

        log.warn("❌ Duplicate account: {}", ex.getMessage());

        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "DUPLICATE_ACCOUNT",
                ex.getMessage(),
                Map.of(
                        "customerId", ex.getCustomerId(),
                        "accountType", ex.getAccountType()
                )
        );
    }

    /**
     * Handle unauthorized access exceptions
     */
    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorizedAccess(
            UnauthorizedAccessException ex, WebRequest request) {

        log.warn("❌ Unauthorized access: {}", ex.getMessage());

        return buildErrorResponse(
                HttpStatus.FORBIDDEN,
                "UNAUTHORIZED_ACCESS",
                ex.getMessage(),
                Map.of(
                        "operation", ex.getOperation(),
                        "resourceId", ex.getResourceId(),
                        "userId", ex.getUserId()
                )
        );
    }

    /**
     * Handle invalid account status exceptions
     */
    @ExceptionHandler(InvalidAccountStatusException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidAccountStatus(
            InvalidAccountStatusException ex, WebRequest request) {

        log.warn("❌ Invalid account status transition: {}", ex.getMessage());

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "INVALID_STATUS_TRANSITION",
                ex.getMessage(),
                Map.of(
                        "accountNumber", ex.getAccountNumber(),
                        "currentStatus", ex.getCurrentStatus(),
                        "requestedStatus", ex.getRequestedStatus()
                )
        );
    }

    /**
     * Handle invalid balance exceptions
     */
    @ExceptionHandler(InvalidBalanceException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidBalance(
            InvalidBalanceException ex, WebRequest request) {

        log.warn("❌ Invalid balance operation: {}", ex.getMessage());

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "INVALID_BALANCE_OPERATION",
                ex.getMessage(),
                Map.of(
                        "requestedBalance", ex.getRequestedBalance(),
                        "reason", ex.getReason()
                )
        );
    }

    /**
     * Handle security exceptions (from JWT interceptor)
     */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, Object>> handleSecurityException(
            SecurityException ex, WebRequest request) {

        log.warn("❌ Security error: {}", ex.getMessage());

        return buildErrorResponse(
                HttpStatus.FORBIDDEN,
                "ACCESS_DENIED",
                ex.getMessage(),
                null
        );
    }

    /**
     * Handle validation errors
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex, WebRequest request) {

        log.warn("❌ Validation error: {}", ex.getMessage());

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_FAILED",
                "Input validation failed",
                Map.of("fieldErrors", fieldErrors)
        );
    }

    /**
     * Handle illegal argument exceptions
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex, WebRequest request) {

        log.warn("❌ Invalid argument: {}", ex.getMessage());

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "INVALID_ARGUMENT",
                ex.getMessage(),
                null
        );
    }

    /**
     * Handle illegal state exceptions
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(
            IllegalStateException ex, WebRequest request) {

        log.error("❌ Invalid state: {}", ex.getMessage());

        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "INVALID_STATE",
                ex.getMessage(),
                null
        );
    }

    /**
     * Handle runtime exceptions
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(
            RuntimeException ex, WebRequest request) {

        log.error("❌ Runtime error: {}", ex.getMessage(), ex);

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "RUNTIME_ERROR",
                "An unexpected error occurred: " + ex.getMessage(),
                null
        );
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex, WebRequest request) {

        log.error("❌ Unexpected error: {}", ex.getMessage(), ex);

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "An unexpected server error occurred",
                null
        );
    }

    /**
     * Build consistent error response format
     */
    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            HttpStatus status, String errorCode, String message, Map<String, Object> details) {

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("code", errorCode);
        errorBody.put("message", message);
        errorBody.put("timestamp", LocalDateTime.now());
        errorBody.put("status", status.value());

        if (details != null && !details.isEmpty()) {
            // Filter out null values from details
            Map<String, Object> filteredDetails = new HashMap<>();
            details.forEach((key, value) -> {
                if (value != null) {
                    filteredDetails.put(key, value);
                }
            });
            if (!filteredDetails.isEmpty()) {
                errorBody.put("details", filteredDetails);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("error", errorBody);

        return ResponseEntity.status(status).body(response);
    }
}