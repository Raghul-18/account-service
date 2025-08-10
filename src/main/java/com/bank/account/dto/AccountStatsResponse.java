package com.bank.account.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountStatsResponse {

    // Overall statistics
    private Long totalAccounts;
    private BigDecimal totalBalance;
    private String formattedTotalBalance;

    // Account type breakdown
    private Long currentAccounts;
    private Long savingsAccounts;

    // Status breakdown
    private Long activeAccounts;
    private Long suspendedAccounts;
    private Long closedAccounts;

    // Balance statistics
    private BigDecimal averageBalance;
    private BigDecimal maxBalance;
    private BigDecimal minBalance;

    // Additional statistics
    private Map<String, Object> additionalStats;
}