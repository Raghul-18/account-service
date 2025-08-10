package com.bank.account.entity;

/**
 * Enum representing different types of bank accounts
 */
public enum AccountType {
    SAVINGS("SAV", "Savings Account", 1000.00),
    CURRENT("CUR", "Current Account", 5000.00);

    private final String code;
    private final String displayName;
    private final Double minimumBalance;

    AccountType(String code, String displayName, Double minimumBalance) {
        this.code = code;
        this.displayName = displayName;
        this.minimumBalance = minimumBalance;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Double getMinimumBalance() {
        return minimumBalance;
    }

    /**
     * Get AccountType from code
     * @param code The account type code (SAV, CUR)
     * @return AccountType or null if not found
     */
    public static AccountType fromCode(String code) {
        for (AccountType type : AccountType.values()) {
            if (type.getCode().equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Check if account type requires minimum balance maintenance
     * @return true if minimum balance is required
     */
    public boolean hasMinimumBalanceRequirement() {
        return minimumBalance > 0;
    }
}