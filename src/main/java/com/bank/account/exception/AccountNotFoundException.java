package com.bank.account.exception;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String message) {
        super(message);
    }

    public AccountNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public static AccountNotFoundException byId(Long accountId) {
        return new AccountNotFoundException("Account not found with ID: " + accountId);
    }

    public static AccountNotFoundException byAccountNumber(String accountNumber) {
        return new AccountNotFoundException("Account not found with account number: " + accountNumber);
    }

    public static AccountNotFoundException byCustomerAndType(Long customerId, String accountType) {
        return new AccountNotFoundException("No " + accountType + " account found for customer: " + customerId);
    }
}