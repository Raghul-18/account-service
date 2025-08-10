package com.bank.account.exception;

public class InvalidAccountOperationException extends RuntimeException {
    public InvalidAccountOperationException(String message) {
        super(message);
    }

    public static InvalidAccountOperationException accountNotActive(String accountNumber) {
        return new InvalidAccountOperationException("Account " + accountNumber + " is not active");
    }

    public static InvalidAccountOperationException kycNotVerified(Long customerId) {
        return new InvalidAccountOperationException("Cannot create account - KYC not verified for customer: " + customerId);
    }

    public static InvalidAccountOperationException invalidAccountType(String accountType) {
        return new InvalidAccountOperationException("Invalid account type: " + accountType);
    }
}