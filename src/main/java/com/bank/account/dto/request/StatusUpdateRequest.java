package com.bank.account.dto.request;

import com.bank.account.entity.AccountStatus;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Request DTO for updating account status
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusUpdateRequest {

    @NotNull(message = "Account status is required")
    private AccountStatus status;

    @NotBlank(message = "Reason for status change is required")
    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    private String reason;

    @Size(max = 100, message = "Updated by field cannot exceed 100 characters")
    private String updatedBy;

    // Optional effective date for status change
    private String effectiveDate;

    // Check if status change requires additional validation
    public boolean requiresBalanceCheck() {
        return status == AccountStatus.CLOSED;
    }
}