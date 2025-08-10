package com.bank.account.entity;

/**
 * Enum representing different states of a bank account
 */
public enum AccountStatus {
    ACTIVE("Active", "Account is active and can be used for transactions"),
    INACTIVE("Inactive", "Account is temporarily inactive, no transactions allowed"),
    CLOSED("Closed", "Account is permanently closed"),
    SUSPENDED("Suspended", "Account is suspended due to security or compliance issues"),
    FROZEN("Frozen", "Account is frozen, balance inquiry allowed but no transactions");

    private final String displayName;
    private final String description;

    AccountStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if account status allows transactions
     * @return true if transactions are allowed
     */
    public boolean allowsTransactions() {
        return this == ACTIVE;
    }

    /**
     * Check if account status allows balance inquiry
     * @return true if balance inquiry is allowed
     */
    public boolean allowsBalanceInquiry() {
        return this == ACTIVE || this == INACTIVE || this == FROZEN;
    }

    /**
     * Check if account can be reactivated
     * @return true if account can be reactivated
     */
    public boolean canBeReactivated() {
        return this == INACTIVE || this == SUSPENDED || this == FROZEN;
    }

    /**
     * Get AccountStatus from string (case-insensitive)
     * @param status Status string
     * @return AccountStatus or null if not found
     */
    public static AccountStatus fromString(String status) {
        if (status == null) {
            return null;
        }
        for (AccountStatus accountStatus : AccountStatus.values()) {
            if (accountStatus.name().equalsIgnoreCase(status)) {
                return accountStatus;
            }
        }
        return null;
    }
}