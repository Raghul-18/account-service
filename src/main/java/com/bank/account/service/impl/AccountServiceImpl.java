package com.bank.account.service.impl;

import com.bank.account.client.CustomerServiceClient;
import com.bank.account.dto.*;
import com.bank.account.entity.Account;
import com.bank.account.entity.AccountStatus;
import com.bank.account.exception.*;
import com.bank.account.repository.AccountRepository;
import com.bank.account.service.AccountService;
import com.bank.account.util.AccountNumberGenerator;
import com.bank.account.util.AccountType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountNumberGenerator accountNumberGenerator;
    private final CustomerServiceClient customerServiceClient;

    @Override
    public AccountResponse createAccount(Long customerId, AccountType accountType, BigDecimal initialBalance) {
        log.info("🏦 Creating {} account for customer {}", accountType, customerId);

        // Check if customer already has this account type
        if (accountRepository.existsByCustomerIdAndAccountType(customerId, accountType)) {
            throw DuplicateAccountException.forCustomerAndType(customerId, accountType);
        }

        // Generate account number
        String accountNumber = generateAccountNumber(accountType);

        // Set default balance if not provided
        if (initialBalance == null) {
            initialBalance = BigDecimal.ZERO;
        }

        // Ensure balance is non-negative and properly scaled
        if (initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidAccountOperationException("Initial balance cannot be negative");
        }
        initialBalance = initialBalance.setScale(2, RoundingMode.HALF_UP);

        // Create account
        Account account = Account.builder()
                .customerId(customerId)
                .accountNumber(accountNumber)
                .accountType(accountType)
                .accountStatus(AccountStatus.ACTIVE)
                .balance(initialBalance)
                .createdAt(LocalDateTime.now())
                .build();

        Account savedAccount = accountRepository.save(account);
        log.info("✅ Created account {} with number {} for customer {}",
                savedAccount.getAccountId(), accountNumber, customerId);

        return toAccountResponse(savedAccount, "Account created successfully");
    }

    @Override
    public AccountResponse createAccount(AccountCreationRequest request) {
        return createAccount(request.getCustomerId(), request.getAccountType(), request.getInitialBalance());
    }

    @Override
    public List<AccountResponse> getAccountsByCustomerId(Long customerId) {
        log.info("🔍 Fetching accounts for customer {}", customerId);

        List<Account> accounts = accountRepository.findByCustomerId(customerId);
        log.info("📄 Found {} accounts for customer {}", accounts.size(), customerId);

        return accounts.stream()
                .map(account -> toAccountResponse(account, null))
                .collect(Collectors.toList());
    }

    @Override
    public AccountResponse getAccountById(Long accountId) {
        log.info("🔍 Fetching account by ID: {}", accountId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> AccountNotFoundException.byId(accountId));

        return toAccountResponse(account, null);
    }

    @Override
    public AccountResponse getAccountByNumber(String accountNumber) {
        log.info("🔍 Fetching account by number: {}", accountNumber);

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> AccountNotFoundException.byAccountNumber(accountNumber));

        return toAccountResponse(account, null);
    }

    @Override
    public AccountResponse getAccountByCustomerAndType(Long customerId, AccountType accountType) {
        log.info("🔍 Fetching {} account for customer {}", accountType, customerId);

        Account account = accountRepository.findByCustomerIdAndAccountType(customerId, accountType)
                .orElseThrow(() -> AccountNotFoundException.byCustomerAndType(customerId, accountType.name()));

        return toAccountResponse(account, null);
    }

    @Override
    public AccountResponse updateAccountStatus(Long accountId, AccountStatusUpdateRequest request) {
        log.info("🔄 Updating account {} status to {}", accountId, request.getAccountStatus());

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> AccountNotFoundException.byId(accountId));

        account.setAccountStatus(request.getAccountStatus());
        Account updatedAccount = accountRepository.save(account);

        String message = String.format("Account status updated to %s", request.getAccountStatus());
        if (request.getReason() != null && !request.getReason().trim().isEmpty()) {
            message += " - " + request.getReason();
        }

        log.info("✅ Updated account {} status to {}", accountId, request.getAccountStatus());
        return toAccountResponse(updatedAccount, message);
    }

    @Override
    public AccountResponse updateAccountBalance(Long accountId, BalanceUpdateRequest request) {
        log.info("🔄 Updating account {} balance to ₹{}", accountId, request.getBalance());

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> AccountNotFoundException.byId(accountId));

        // Ensure balance is properly scaled
        BigDecimal newBalance = request.getBalance().setScale(2, RoundingMode.HALF_UP);
        account.setBalance(newBalance);
        Account updatedAccount = accountRepository.save(account);

        String message = String.format("Balance updated to ₹%,.2f", newBalance);
        if (request.getReason() != null && !request.getReason().trim().isEmpty()) {
            message += " - " + request.getReason();
        }

        log.info("✅ Updated account {} balance to ₹{}", accountId, newBalance);
        return toAccountResponse(updatedAccount, message);
    }

    @Override
    public boolean verifyAccountOwnership(Long accountId, Long customerId) {
        return accountRepository.existsByAccountIdAndCustomerId(accountId, customerId);
    }

    @Override
    public List<AccountResponse> getAllAccounts() {
        log.info("🔍 Fetching all accounts (admin operation)");

        List<Account> accounts = accountRepository.findAll();
        log.info("📄 Found {} total accounts", accounts.size());

        return accounts.stream()
                .map(account -> toAccountResponse(account, null))
                .collect(Collectors.toList());
    }

    @Override
    public List<AccountResponse> getAccountsByStatus(String status) {
        log.info("🔍 Fetching accounts with status: {}", status);

        AccountStatus accountStatus = AccountStatus.valueOf(status.toUpperCase());
        List<Account> accounts = accountRepository.findByAccountStatus(accountStatus);
        log.info("📄 Found {} accounts with status {}", accounts.size(), status);

        return accounts.stream()
                .map(account -> toAccountResponse(account, null))
                .collect(Collectors.toList());
    }

    @Override
    public AccountStatsResponse getAccountStatistics() {
        log.info("📊 Generating account statistics");

        List<Account> allAccounts = accountRepository.findAll();
        BigDecimal totalBalance = accountRepository.getTotalActiveBalance();
        if (totalBalance == null) totalBalance = BigDecimal.ZERO;

        // Calculate statistics
        long totalAccounts = allAccounts.size();
        long currentAccounts = allAccounts.stream()
                .filter(a -> a.getAccountType() == AccountType.CURRENT).count();
        long savingsAccounts = allAccounts.stream()
                .filter(a -> a.getAccountType() == AccountType.SAVINGS).count();

        long activeAccounts = allAccounts.stream()
                .filter(a -> a.getAccountStatus() == AccountStatus.ACTIVE).count();
        long suspendedAccounts = allAccounts.stream()
                .filter(a -> a.getAccountStatus() == AccountStatus.SUSPENDED).count();
        long closedAccounts = allAccounts.stream()
                .filter(a -> a.getAccountStatus() == AccountStatus.CLOSED).count();

        // Balance statistics
        BigDecimal averageBalance = totalAccounts > 0 ?
                totalBalance.divide(BigDecimal.valueOf(totalAccounts), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        BigDecimal maxBalance = allAccounts.stream()
                .map(Account::getBalance)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal minBalance = allAccounts.stream()
                .map(Account::getBalance)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        AccountStatsResponse stats = AccountStatsResponse.builder()
                .totalAccounts(totalAccounts)
                .totalBalance(totalBalance)
                .formattedTotalBalance(String.format("₹%,.2f", totalBalance))
                .currentAccounts(currentAccounts)
                .savingsAccounts(savingsAccounts)
                .activeAccounts(activeAccounts)
                .suspendedAccounts(suspendedAccounts)
                .closedAccounts(closedAccounts)
                .averageBalance(averageBalance)
                .maxBalance(maxBalance)
                .minBalance(minBalance)
                .build();

        log.info("📊 Account Statistics: {} total, ₹{} total balance", totalAccounts, totalBalance);
        return stats;
    }

    @Override
    public void createAccountsForKycCompletedCustomer(Long customerId) {
        log.info("🎉 Creating accounts for KYC completed customer: {}", customerId);

        try {
            // Create both CURRENT and SAVINGS accounts
            createAccount(customerId, AccountType.CURRENT, BigDecimal.ZERO);
            createAccount(customerId, AccountType.SAVINGS, BigDecimal.ZERO);

            log.info("✅ Successfully created both accounts for customer: {}", customerId);
        } catch (DuplicateAccountException e) {
            log.warn("⚠️ Some accounts already exist for customer {}: {}", customerId, e.getMessage());
        } catch (Exception e) {
            log.error("❌ Failed to create accounts for customer {}: {}", customerId, e.getMessage(), e);
            throw new RuntimeException("Failed to create accounts for customer: " + customerId, e);
        }
    }

    @Override
    public String generateAccountNumber(AccountType accountType) {
        Long sequence = accountRepository.countByAccountType(accountType) + 1;
        return accountNumberGenerator.generateAccountNumber(accountType, sequence);
    }

    // Helper method to convert Account entity to AccountResponse DTO
    private AccountResponse toAccountResponse(Account account, String message) {
        return AccountResponse.builder()
                .accountId(account.getAccountId())
                .customerId(account.getCustomerId())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType())
                .accountStatus(account.getAccountStatus())
                .balance(account.getBalance())
                .formattedBalance(account.getFormattedBalance())
                .createdAt(account.getCreatedAt())
                .message(message)
                .build();
    }
}