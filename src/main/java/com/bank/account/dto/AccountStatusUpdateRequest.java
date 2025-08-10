package com.bank.account.dto;

import com.bank.account.entity.AccountStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountStatusUpdateRequest {

    @NotNull(message = "Account status is required")
    private AccountStatus accountStatus;

    private String reason; // Optional reason for status change
}