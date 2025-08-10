package com.bank.account.exception;

import com.bank.account.entity.AccountStatus;

/**
 * Exception thrown when attempting invalid account status transitions
 */
public class InvalidAccountStatusException extends RuntimeException {

    private final String accountNumber;
    private final AccountStatus currentStatus;
    private final AccountStatus requestedStatus;

    public InvalidAccountStatusException(String accountNumber, AccountStatus currentStatus, AccountStatus requestedStatus) {
        super(String.format("Invalid status transition for account %s: %s -> %s",
                accountNumber, currentStatus, requestedStatus));
        this.accountNumber = accountNumber;
        this.currentStatus = currentStatus;
        this.requestedStatus = requestedStatus;
    }

    public InvalidAccountStatusException(String message, String accountNumber,
                                         AccountStatus currentStatus, AccountStatus requestedStatus) {
        super(message);
        this.accountNumber = accountNumber;
        this.currentStatus = currentStatus;
        this.requestedStatus = requestedStatus;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public AccountStatus getCurrentStatus() {
        return currentStatus;
    }

    public AccountStatus getRequestedStatus() {
        return requestedStatus;
    }
}