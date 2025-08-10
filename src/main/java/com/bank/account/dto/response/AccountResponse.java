package com.bank.account.dto.response;

import com.bank.account.entity.AccountStatus;
import com.bank.account.entity.AccountType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for account details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountResponse {

    private Long accountId;
    private Long customerId;
    private String accountNumber;
    private AccountType accountType;
    private String accountTypeDisplay;
    private AccountStatus accountStatus;
    private String accountStatusDisplay;
    private BigDecimal balance;
    private BigDecimal availableBalance;
    private BigDecimal minimumBalance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String message;

    // Helper methods for display
    public String getFormattedBalance() {
        return balance != null ? String.format("₹%.2f", balance) : "₹0.00";
    }

    public String getAccountSummary() {
        return String.format("%s - %s (%s)",
                accountNumber,
                accountTypeDisplay,
                accountStatusDisplay);
    }

    public boolean isLowBalance() {
        return balance != null && minimumBalance != null &&
                balance.compareTo(minimumBalance) < 0;
    }

    public boolean canPerformTransactions() {
        return accountStatus != null && accountStatus.allowsTransactions();
    }
}