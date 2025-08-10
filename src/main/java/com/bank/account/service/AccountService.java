package com.bank.account.service;

import com.bank.account.dto.*;
import com.bank.account.util.AccountType;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {

    // Account creation
    AccountResponse createAccount(Long customerId, AccountType accountType, BigDecimal initialBalance);
    AccountResponse createAccount(AccountCreationRequest request);

    // Account retrieval
    List<AccountResponse> getAccountsByCustomerId(Long customerId);
    AccountResponse getAccountById(Long accountId);
    AccountResponse getAccountByNumber(String accountNumber);
    AccountResponse getAccountByCustomerAndType(Long customerId, AccountType accountType);

    // Account management
    AccountResponse updateAccountStatus(Long accountId, AccountStatusUpdateRequest request);
    AccountResponse updateAccountBalance(Long accountId, BalanceUpdateRequest request);

    // Access control
    boolean verifyAccountOwnership(Long accountId, Long customerId);

    // Admin operations
    List<AccountResponse> getAllAccounts();
    List<AccountResponse> getAccountsByStatus(String status);
    AccountStatsResponse getAccountStatistics();

    // Automatic account creation (Kafka integration - Phase 2)
    void createAccountsForKycCompletedCustomer(Long customerId);

    // Account number generation
    String generateAccountNumber(AccountType accountType);
}