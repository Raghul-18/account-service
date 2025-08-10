package com.bank.account.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Response DTO for account statistics (Admin dashboard)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountStatsResponse {

    // Overall statistics
    private Long totalAccounts;
    private Long activeAccounts;
    private Long inactiveAccounts;
    private Long closedAccounts;

    // Account type breakdown
    private Long savingsAccounts;
    private Long currentAccounts;

    // Balance statistics
    private BigDecimal totalBalance;
    private BigDecimal averageBalance;
    private BigDecimal highestBalance;
    private BigDecimal lowestBalance;

    // Recent activity
    private Long accountsCreatedToday;
    private Long accountsCreatedThisWeek;
    private Long accountsCreatedThisMonth;

    // Status distribution
    private Map<String, Long> statusDistribution;
    private Map<String, Long> typeDistribution;

    // Health indicators
    private Long lowBalanceAccounts;
    private Long dormantAccounts;
    private BigDecimal totalMinimumBalanceShortfall;

    public String getFormattedTotalBalance() {
        return totalBalance != null ? String.format("₹%.2f", totalBalance) : "₹0.00";
    }

    public String getFormattedAverageBalance() {
        return averageBalance != null ? String.format("₹%.2f", averageBalance) : "₹0.00";
    }

    public double getActiveAccountsPercentage() {
        if (totalAccounts == null || totalAccounts == 0) return 0.0;
        return (activeAccounts != null ? activeAccounts.doubleValue() : 0.0) / totalAccounts * 100;
    }

    public double getLowBalancePercentage() {
        if (totalAccounts == null || totalAccounts == 0) return 0.0;
        return (lowBalanceAccounts != null ? lowBalanceAccounts.doubleValue() : 0.0) / totalAccounts * 100;
    }
}