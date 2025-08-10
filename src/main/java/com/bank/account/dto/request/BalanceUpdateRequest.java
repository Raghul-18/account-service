package com.bank.account.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Request DTO for updating account balance (Admin only)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BalanceUpdateRequest {

    @NotNull(message = "New balance is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Balance cannot be negative")
    @Digits(integer = 13, fraction = 2, message = "Invalid balance format")
    private BigDecimal newBalance;

    @NotBlank(message = "Reason for balance update is required")
    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    private String reason;

    @Size(max = 100, message = "Updated by field cannot exceed 100 characters")
    private String updatedBy;

    // Optional reference number for tracking
    @Size(max = 50, message = "Reference number cannot exceed 50 characters")
    private String referenceNumber;
}