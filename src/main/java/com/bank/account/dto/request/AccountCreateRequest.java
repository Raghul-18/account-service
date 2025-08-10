package com.bank.account.dto.request;

import com.bank.account.entity.AccountType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Request DTO for creating a new account
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountCreateRequest {

    @NotNull(message = "Customer ID is required")
    @Positive(message = "Customer ID must be positive")
    private Long customerId;

    @NotNull(message = "Account type is required")
    private AccountType accountType;

    @NotNull(message = "Initial balance is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Initial balance cannot be negative")
    @Digits(integer = 13, fraction = 2, message = "Invalid balance format")
    private BigDecimal initialBalance;

    @Size(max = 255, message = "Remarks cannot exceed 255 characters")
    private String remarks;

    // Validation method to check if initial balance meets minimum requirement
    public boolean isInitialBalanceValid() {
        if (accountType == null || initialBalance == null) {
            return false;
        }
        return initialBalance.compareTo(BigDecimal.valueOf(accountType.getMinimumBalance())) >= 0;
    }
}