package com.bank.account.exception;

import com.bank.account.entity.AccountType;

/**
 * Exception thrown when attempting to create duplicate account type for a customer
 */
public class DuplicateAccountException extends RuntimeException {

    private final Long customerId;
    private final AccountType accountType;

    public DuplicateAccountException(Long customerId, AccountType accountType) {
        super(String.format("Customer %d already has a %s account", customerId, accountType.getDisplayName()));
        this.customerId = customerId;
        this.accountType = accountType;
    }

    public DuplicateAccountException(String message, Long customerId, AccountType accountType) {
        super(message);
        this.customerId = customerId;
        this.accountType = accountType;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public AccountType getAccountType() {
        return accountType;
    }
}