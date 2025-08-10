package com.bank.account.mapper;

import com.bank.account.dto.response.*;
import com.bank.account.entity.Account;
import com.bank.account.entity.AccountType;
import com.bank.account.entity.AccountStatus;
import com.bank.account.util.Constants;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Simplified AccountMapper to resolve compilation issues
 */
@Mapper(componentModel = "spring")
public interface AccountMapper {

    /**
     * Map Account entity to AccountResponse (basic mapping)
     */
    @Mapping(target = "availableBalance", source = "balance")
    @Mapping(target = "minimumBalance", expression = "java(getMinimumBalanceForType(account.getAccountType()))")
    @Mapping(target = "message", source = "message")
    AccountResponse toAccountResponse(Account account, String message);

    /**
     * Map Account entity to BalanceResponse (basic mapping)
     */
    @Mapping(target = "availableBalance", source = "balance")
    @Mapping(target = "minimumBalance", expression = "java(getMinimumBalanceForType(account.getAccountType()))")
    @Mapping(target = "canDebit", expression = "java(account.canDebit())")
    @Mapping(target = "canCredit", expression = "java(account.canCredit())")
    @Mapping(target = "message", source = "message")
    BalanceResponse toBalanceResponse(Account account, String message);

    /**
     * Helper method for minimum balance
     */
    default BigDecimal getMinimumBalanceForType(AccountType accountType) {
        if (accountType == AccountType.SAVINGS) {
            return Constants.Account.SAVINGS_MIN_BALANCE;
        } else if (accountType == AccountType.CURRENT) {
            return Constants.Account.CURRENT_MIN_BALANCE;
        }
        return BigDecimal.ZERO;
    }

    /**
     * Manual implementation for CustomerAccountSummaryResponse
     */
    default CustomerAccountSummaryResponse toCustomerAccountSummaryResponse(List<Account> accounts, String message) {
        if (accounts == null || accounts.isEmpty()) {
            return CustomerAccountSummaryResponse.builder()
                    .totalAccounts(0)
                    .totalBalance(BigDecimal.ZERO)
                    .savingsBalance(BigDecimal.ZERO)
                    .currentBalance(BigDecimal.ZERO)
                    .hasActiveAccounts(false)
                    .hasSavingsAccount(false)
                    .hasCurrentAccount(false)
                    .message(message != null ? message : "No accounts found")
                    .build();
        }

        Long customerId = accounts.get(0).getCustomerId();

        BigDecimal totalBalance = accounts.stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal savingsBalance = accounts.stream()
                .filter(acc -> acc.getAccountType() == AccountType.SAVINGS)
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal currentBalance = accounts.stream()
                .filter(acc -> acc.getAccountType() == AccountType.CURRENT)
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        boolean hasActiveAccounts = accounts.stream()
                .anyMatch(acc -> acc.getAccountStatus() == AccountStatus.ACTIVE);

        boolean hasSavingsAccount = accounts.stream()
                .anyMatch(acc -> acc.getAccountType() == AccountType.SAVINGS);

        boolean hasCurrentAccount = accounts.stream()
                .anyMatch(acc -> acc.getAccountType() == AccountType.CURRENT);

        return CustomerAccountSummaryResponse.builder()
                .customerId(customerId)
                .totalAccounts(accounts.size())
                .totalBalance(totalBalance)
                .savingsBalance(savingsBalance)
                .currentBalance(currentBalance)
                .hasActiveAccounts(hasActiveAccounts)
                .hasSavingsAccount(hasSavingsAccount)
                .hasCurrentAccount(hasCurrentAccount)
                .message(message != null ? message : "Customer account summary retrieved successfully")
                .build();
    }
}