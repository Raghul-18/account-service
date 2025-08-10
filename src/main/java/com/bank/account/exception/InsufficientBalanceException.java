package com.bank.account.exception;

import java.math.BigDecimal;

public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(String message) {
        super(message);
    }

    public static InsufficientBalanceException create(BigDecimal currentBalance, BigDecimal requiredAmount) {
        return new InsufficientBalanceException(
                String.format("Insufficient balance. Current: ₹%,.2f, Required: ₹%,.2f",
                        currentBalance, requiredAmount)
        );
    }
}
