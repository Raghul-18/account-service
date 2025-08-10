package com.bank.account.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for list of accounts
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountListResponse {

    private List<AccountResponse> accounts;
    private int totalAccounts;
    private BigDecimal totalBalance;
    private BigDecimal totalAvailableBalance;
    private String message;

    // Pagination info (if applicable)
    private Integer currentPage;
    private Integer totalPages;
    private Integer pageSize;

    // Summary information
    public String getBalanceSummary() {
        return String.format("Total Balance: ₹%.2f across %d accounts",
                totalBalance != null ? totalBalance : BigDecimal.ZERO,
                totalAccounts);
    }

    public boolean hasAccounts() {
        return accounts != null && !accounts.isEmpty();
    }

    public long getActiveAccountsCount() {
        if (accounts == null) return 0;
        return accounts.stream()
                .filter(AccountResponse::canPerformTransactions)
                .count();
    }
}