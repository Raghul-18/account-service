package com.bank.account.exception;

import com.bank.account.util.AccountType;

public class DuplicateAccountException extends RuntimeException {
    public DuplicateAccountException(String message) {
        super(message);
    }

    public static DuplicateAccountException forCustomerAndType(Long customerId, AccountType accountType) {
        return new DuplicateAccountException(
                String.format("Customer %d already has a %s account", customerId, accountType.name())
        );
    }
}