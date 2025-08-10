package com.bank.account.dto.response;

import com.bank.account.entity.AccountType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for balance inquiry
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BalanceResponse {

    private String accountNumber;
    private AccountType accountType;
    private BigDecimal currentBalance;
    private BigDecimal availableBalance;
    private BigDecimal minimumBalance;
    private LocalDateTime lastUpdated;
    private String message;

    // Balance status indicators
    private boolean isLowBalance;
    private boolean canDebit;
    private BigDecimal maxWithdrawalAmount;

    public String getFormattedCurrentBalance() {
        return currentBalance != null ? String.format("₹%.2f", currentBalance) : "₹0.00";
    }

    public String getFormattedAvailableBalance() {
        return availableBalance != null ? String.format("₹%.2f", availableBalance) : "₹0.00";
    }

    public String getBalanceStatus() {
        if (isLowBalance) {
            return "Low Balance";
        } else if (currentBalance != null && currentBalance.compareTo(minimumBalance.multiply(BigDecimal.valueOf(2))) > 0) {
            return "Good Balance";
        } else {
            return "Normal Balance";
        }
    }
}