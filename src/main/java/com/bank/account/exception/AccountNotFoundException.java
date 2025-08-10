package com.bank.account.exception;

/**
 * Exception thrown when an account is not found
 */
public class AccountNotFoundException extends RuntimeException {

    private final Long accountId;
    private final String accountNumber;

    public AccountNotFoundException(Long accountId) {
        super("Account not found with ID: " + accountId);
        this.accountId = accountId;
        this.accountNumber = null;
    }

    public AccountNotFoundException(String accountNumber) {
        super("Account not found with number: " + accountNumber);
        this.accountId = null;
        this.accountNumber = accountNumber;
    }

    public AccountNotFoundException(String message, Long accountId) {
        super(message);
        this.accountId = accountId;
        this.accountNumber = null;
    }

    public Long getAccountId() {
        return accountId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }
}