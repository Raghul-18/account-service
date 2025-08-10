package com.bank.account.dto;

import com.bank.account.entity.AccountStatus;
import com.bank.account.util.AccountType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountResponse {
    private Long accountId;
    private Long customerId;
    private String accountNumber;
    private AccountType accountType;
    private AccountStatus accountStatus;
    private BigDecimal balance;
    private String formattedBalance; // ₹1,234.56
    private LocalDateTime createdAt;
    private String message; // Success/error messages
}