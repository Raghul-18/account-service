package com.bank.account.dto.request;

import com.bank.account.entity.AccountStatus;
import com.bank.account.entity.AccountType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request DTO for filtering accounts (Admin queries)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountFilterRequest {

    private AccountType accountType;
    private AccountStatus accountStatus;
    private Long customerId;

    // Balance filters
    private BigDecimal minBalance;
    private BigDecimal maxBalance;

    // Date filters
    private LocalDateTime createdAfter;
    private LocalDateTime createdBefore;

    // Pagination
    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 20;

    // Sorting
    @Builder.Default
    private String sortBy = "createdAt";

    @Builder.Default
    private String sortDirection = "DESC";

    public boolean hasFilters() {
        return accountType != null || accountStatus != null || customerId != null ||
                minBalance != null || maxBalance != null ||
                createdAfter != null || createdBefore != null;
    }
}