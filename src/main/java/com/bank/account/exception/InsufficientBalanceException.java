package com.bank.account.exception;

import java.math.BigDecimal;

/**
 * Exception thrown when account has insufficient balance for an operation
 */
public class InsufficientBalanceException extends RuntimeException {

    private final String accountNumber;
    private final BigDecimal currentBalance;
    private final BigDecimal requestedAmount;
    private final BigDecimal minimumBalance;

    public InsufficientBalanceException(String accountNumber, BigDecimal currentBalance,
                                        BigDecimal requestedAmount, BigDecimal minimumBalance) {
        super(String.format("Insufficient balance in account %s. Current: %.2f, Requested: %.2f, Minimum required: %.2f",
                accountNumber, currentBalance, requestedAmount, minimumBalance));
        this.accountNumber = accountNumber;
        this.currentBalance = currentBalance;
        this.requestedAmount = requestedAmount;
        this.minimumBalance = minimumBalance;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }

    public BigDecimal getMinimumBalance() {
        return minimumBalance;
    }
}