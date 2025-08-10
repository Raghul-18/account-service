package com.bank.account.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for customer's account summary
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerAccountSummaryResponse {

    private Long customerId;
    private List<AccountResponse> accounts;
    private BigDecimal totalBalance;
    private int totalAccounts;

    // Quick access to specific account types
    private AccountResponse savingsAccount;
    private AccountResponse currentAccount;

    // Summary flags
    private boolean hasSavingsAccount;
    private boolean hasCurrentAccount;
    private boolean canCreateSavingsAccount;
    private boolean canCreateCurrentAccount;
    private boolean hasLowBalanceAccounts;

    private String message;

    public String getCustomerAccountSummary() {
        return String.format("Customer has %d account(s) with total balance of ₹%.2f",
                totalAccounts,
                totalBalance != null ? totalBalance : BigDecimal.ZERO);
    }

    public boolean hasActiveAccounts() {
        return accounts != null && accounts.stream()
                .anyMatch(AccountResponse::canPerformTransactions);
    }
}