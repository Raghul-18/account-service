package com.bank.account.exception;

import java.math.BigDecimal;

/**
 * Exception thrown for invalid balance operations
 */
public class InvalidBalanceException extends RuntimeException {

    private final BigDecimal requestedBalance;
    private final String reason;

    public InvalidBalanceException(String message) {
        super(message);
        this.requestedBalance = null;
        this.reason = null;
    }

    public InvalidBalanceException(BigDecimal requestedBalance, String reason) {
        super(String.format("Invalid balance operation: %.2f - %s", requestedBalance, reason));
        this.requestedBalance = requestedBalance;
        this.reason = reason;
    }

    public BigDecimal getRequestedBalance() {
        return requestedBalance;
    }

    public String getReason() {
        return reason;
    }
}