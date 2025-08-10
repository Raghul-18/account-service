package com.bank.account.util;

public enum AccountType {
    CURRENT("CUR"),
    SAVINGS("SAV");

    private final String code;

    AccountType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static AccountType fromString(String type) {
        return AccountType.valueOf(type.toUpperCase());
    }
}
