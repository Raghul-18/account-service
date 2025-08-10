package com.bank.account.service.impl;

import com.bank.account.client.CustomerServiceClient;
import com.bank.account.client.KycServiceClient;
import com.bank.account.dto.request.*;
import com.bank.account.dto.response.*;
import com.bank.account.entity.Account;
import com.bank.account.entity.AccountStatus;
import com.bank.account.entity.AccountType;
import com.bank.account.exception.*;
import com.bank.account.mapper.AccountMapper;
import com.bank.account.repository.AccountRepository;
import com.bank.account.service.AccountNumberGenerator;
import com.bank.account.service.AccountService;
import com.bank.account.util.AuthenticatedUser;
import com.bank.account.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of AccountService with comprehensive business logic
 * Handles all account operations with proper validation and authorization
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountNumberGenerator accountNumberGenerator;
    private final AccountMapper accountMapper;
    private final CustomerServiceClient customerServiceClient;
    private final KycServiceClient kycServiceClient;

    @Value("${account.default-balance.savings:1000.00}")
    private BigDecimal defaultSavingsBalance;

    @Value("${account.default-balance.current:5000.00}")
    private BigDecimal defaultCurrentBalance;

    // ==================== CUSTOMER OPERATIONS ====================

    @Override
    @Transactional
    public AccountResponse createAccount(AccountCreateRequest request) {
        log.info("Creating account for customer: {}, type: {}", request.getCustomerId(), request.getAccountType());

        // Authorization check - customers can only create accounts for themselves
        if (!isAdminUser() && !request.getCustomerId().equals(getCurrentUserId())) {
            throw new UnauthorizedAccessException(
                    "Customers can only create accounts for themselves"
            );
        }

        // Validate customer exists
        validateCustomerExists(request.getCustomerId());

        // Check if customer already has this account type
        if (accountRepository.existsByCustomerIdAndAccountType(request.getCustomerId(), request.getAccountType())) {
            throw new DuplicateAccountException(
                    String.format("Customer %d already has a %s account",
                            request.getCustomerId(), request.getAccountType())
            );
        }

        // Validate initial balance meets minimum requirement
        BigDecimal minimumBalance = getMinimumBalance(request.getAccountType());
        if (request.getInitialBalance().compareTo(minimumBalance) < 0) {
            throw new InvalidBalanceException(
                    String.format("Initial balance %.2f is below minimum required %.2f for %s account",
                            request.getInitialBalance(), minimumBalance, request.getAccountType())
            );
        }

        // Generate unique account number
        String accountNumber = accountNumberGenerator.generateAccountNumber(
                request.getCustomerId(), request.getAccountType()
        );

        // Create account entity
        Account account = Account.builder()
                .customerId(request.getCustomerId())
                .accountNumber(accountNumber)
                .accountType(request.getAccountType())
                .accountStatus(AccountStatus.ACTIVE)
                .balance(request.getInitialBalance())
                .build();

        account = accountRepository.save(account);
        log.info("Successfully created account: {} for customer: {}", accountNumber, request.getCustomerId());

        return accountMapper.toAccountResponse(account, "Account created successfully");
    }

    @Override
    public AccountResponse getAccountById(Long accountId) {
        log.debug("Fetching account by ID: {}", accountId);

        Account account = findAccountById(accountId);
        validateAccountAccess(account);

        return accountMapper.toAccountResponse(account, "Account retrieved successfully");
    }

    @Override
    public AccountListResponse getAccountsByCustomerId(Long customerId) {
        log.debug("Fetching accounts for customer: {}", customerId);

        // Authorization check - customers can only see their own accounts
        if (!isAdminUser() && !customerId.equals(getCurrentUserId())) {
            throw new UnauthorizedAccessException(
                    "Customers can only view their own accounts"
            );
        }

        List<Account> accounts = accountRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
        List<AccountResponse> accountResponses = accounts.stream()
                .map(account -> accountMapper.toAccountResponse(account, null))
                .collect(Collectors.toList());

        BigDecimal totalBalance = accounts.stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return AccountListResponse.builder()
                .accounts(accountResponses)
                .totalAccounts(accounts.size())
                .totalBalance(totalBalance)
                .message("Customer accounts retrieved successfully")
                .build();
    }

    @Override
    public BalanceResponse getAccountBalance(Long accountId) {
        log.debug("Fetching balance for account: {}", accountId);

        Account account = findAccountById(accountId);
        validateAccountAccess(account);

        return accountMapper.toBalanceResponse(account, "Balance retrieved successfully");
    }

    @Override
    @Transactional
    public AccountResponse updateAccountStatus(Long accountId, StatusUpdateRequest request) {
        log.info("Updating account status: {} to {}", accountId, request.getNewStatus());

        Account account = findAccountById(accountId);
        validateAccountAccess(account);

        // Customers can only activate/deactivate, not close or freeze
        if (!isAdminUser()) {
            validateCustomerStatusUpdate(account.getAccountStatus(), request.getNewStatus());
        }

        // Validate status transition
        validateStatusTransition(account.getAccountStatus(), request.getNewStatus());

        // Update status
        account.setAccountStatus(request.getNewStatus());
        account.setUpdatedAt(LocalDateTime.now());

        account = accountRepository.save(account);
        log.info("Successfully updated account {} status to {}", accountId, request.getNewStatus());

        return accountMapper.toAccountResponse(account, "Account status updated successfully");
    }

    @Override
    public CustomerAccountSummaryResponse getCustomerAccountSummary(Long customerId) {
        log.debug("Fetching account summary for customer: {}", customerId);

        // Authorization check
        if (!isAdminUser() && !customerId.equals(getCurrentUserId())) {
            throw new UnauthorizedAccessException(
                    "Customers can only view their own account summary"
            );
        }

        List<Account> accounts = accountRepository.findByCustomerId(customerId);

        return accountMapper.toCustomerAccountSummaryResponse(accounts, "Account summary retrieved successfully");
    }

    // ==================== ADMIN OPERATIONS ====================

    @Override
    public AccountListResponse getAllAccounts(AccountFilterRequest filterRequest) {
        log.debug("Admin fetching all accounts with filter: {}", filterRequest);

        validateAdminAccess();

        Pageable pageable = PageRequest.of(
                filterRequest.getPage(),
                filterRequest.getSize(),
                Sort.by(Sort.Direction.fromString(filterRequest.getSortDirection()), filterRequest.getSortBy())
        );

        Page<Account> accountPage;

        // Apply filters
        if (filterRequest.getAccountStatus() != null && filterRequest.getAccountType() != null) {
            accountPage = accountRepository.findByAccountStatusAndAccountType(
                    filterRequest.getAccountStatus(), filterRequest.getAccountType(), pageable);
        } else if (filterRequest.getAccountStatus() != null) {
            accountPage = accountRepository.findByAccountStatus(filterRequest.getAccountStatus(), pageable);
        } else if (filterRequest.getAccountType() != null) {
            accountPage = accountRepository.findByAccountType(filterRequest.getAccountType(), pageable);
        } else {
            accountPage = accountRepository.findAll(pageable);
        }

        List<AccountResponse> accountResponses = accountPage.getContent().stream()
                .map(account -> accountMapper.toAccountResponse(account, null))
                .collect(Collectors.toList());

        BigDecimal totalBalance = accountPage.getContent().stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return AccountListResponse.builder()
                .accounts(accountResponses)
                .totalAccounts((int) accountPage.getTotalElements())
                .totalBalance(totalBalance)
                .currentPage(filterRequest.getPage())
                .totalPages(accountPage.getTotalPages())
                .hasNext(accountPage.hasNext())
                .hasPrevious(accountPage.hasPrevious())
                .message("All accounts retrieved successfully")
                .build();
    }

    @Override
    @Transactional
    public AccountResponse updateAccountBalance(Long accountId, BalanceUpdateRequest request) {
        log.info("Admin updating balance for account: {} to {}", accountId, request.getNewBalance());

        validateAdminAccess();

        Account account = findAccountById(accountId);

        // Validate new balance
        if (request.getNewBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidBalanceException("Balance cannot be negative");
        }

        // Check minimum balance requirement
        BigDecimal minimumBalance = getMinimumBalance(account.getAccountType());
        if (request.getNewBalance().compareTo(minimumBalance) < 0) {
            log.warn("Admin setting balance below minimum for account: {}", accountId);
        }

        // Validate account is not closed
        if (account.getAccountStatus() == AccountStatus.CLOSED) {
            throw new InvalidAccountStatusException("Cannot update balance of closed account");
        }

        account.setBalance(request.getNewBalance());
        account.setUpdatedAt(LocalDateTime.now());

        account = accountRepository.save(account);
        log.info("Successfully updated account {} balance to {}", accountId, request.getNewBalance());

        return accountMapper.toAccountResponse(account,
                String.format("Balance updated successfully. Reason: %s", request.getReason()));
    }

    @Override
    @Transactional
    public AccountResponse forceUpdateAccountStatus(Long accountId, StatusUpdateRequest request) {
        log.info("Admin force updating account status: {} to {}", accountId, request.getNewStatus());

        validateAdminAccess();

        Account account = findAccountById(accountId);

        // Admin can perform any status transition
        account.setAccountStatus(request.getNewStatus());
        account.setUpdatedAt(LocalDateTime.now());

        account = accountRepository.save(account);
        log.info("Successfully force updated account {} status to {}", accountId, request.getNewStatus());

        return accountMapper.toAccountResponse(account,
                String.format("Status updated successfully. Reason: %s", request.getReason()));
    }

    @Override
    public AccountStatsResponse getAccountStatistics() {
        log.debug("Admin fetching account statistics");

        validateAdminAccess();

        return AccountStatsResponse.builder()
                .totalAccounts(accountRepository.count())
                .activeAccounts(accountRepository.countByAccountStatus(AccountStatus.ACTIVE))
                .inactiveAccounts(accountRepository.countByAccountStatus(AccountStatus.INACTIVE))
                .closedAccounts(accountRepository.countByAccountStatus(AccountStatus.CLOSED))
                .suspendedAccounts(accountRepository.countByAccountStatus(AccountStatus.SUSPENDED))
                .frozenAccounts(accountRepository.countByAccountStatus(AccountStatus.FROZEN))
                .savingsAccounts(accountRepository.countByAccountType(AccountType.SAVINGS))
                .currentAccounts(accountRepository.countByAccountType(AccountType.CURRENT))
                .totalBalance(accountRepository.getTotalBalance())
                .totalCustomers(accountRepository.getUniqueCustomerCount())
                .averageBalancePerAccount(accountRepository.getAverageBalance())
                .averageBalancePerCustomer(accountRepository.getAverageBalancePerCustomer())
                .message("Account statistics retrieved successfully")
                .build();
    }

    @Override
    public List<AccountResponse> getAccountsByStatus(AccountStatus status) {
        log.debug("Admin fetching accounts by status: {}", status);

        validateAdminAccess();

        List<Account> accounts = accountRepository.findByAccountStatus(status);
        return accounts.stream()
                .map(account -> accountMapper.toAccountResponse(account, null))
                .collect(Collectors.toList());
    }

    @Override
    public List<AccountResponse> getAccountsByType(AccountType accountType) {
        log.debug("Admin fetching accounts by type: {}", accountType);

        validateAdminAccess();

        List<Account> accounts = accountRepository.findByAccountType(accountType);
        return accounts.stream()
                .map(account -> accountMapper.toAccountResponse(account, null))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteAccount(Long accountId) {
        log.info("Admin deleting account: {}", accountId);

        validateAdminAccess();

        Account account = findAccountById(accountId);

        // Validate account can be deleted
        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new InvalidBalanceException("Cannot delete account with non-zero balance");
        }

        if (account.getAccountStatus() != AccountStatus.CLOSED) {
            throw new InvalidAccountStatusException("Can only delete closed accounts");
        }

        accountRepository.delete(account);
        log.info("Successfully deleted account: {}", accountId);
    }

    // ==================== INTERNAL/INTER-SERVICE OPERATIONS ====================

    @Override
    @Transactional
    public List<AccountResponse> createAccountsAfterKycVerification(Long customerId) {
        log.info("Auto-creating accounts after KYC verification for customer: {}", customerId);

        // Validate customer exists and KYC is verified
        validateCustomerExists(customerId);
        validateKycStatus(customerId);

        // Check if accounts already exist
        List<Account> existingAccounts = accountRepository.findByCustomerId(customerId);
        if (!existingAccounts.isEmpty()) {
            log.warn("Customer {} already has accounts, skipping auto-creation", customerId);
            return existingAccounts.stream()
                    .map(account -> accountMapper.toAccountResponse(account, null))
                    .collect(Collectors.toList());
        }

        // Create SAVINGS account
        Account savingsAccount = createAccountInternal(customerId, AccountType.SAVINGS, defaultSavingsBalance);

        // Create CURRENT account
        Account currentAccount = createAccountInternal(customerId, AccountType.CURRENT, defaultCurrentBalance);

        log.info("Successfully created auto accounts for customer: {}", customerId);

        return List.of(
                accountMapper.toAccountResponse(savingsAccount, "SAVINGS account created after KYC verification"),
                accountMapper.toAccountResponse(currentAccount, "CURRENT account created after KYC verification")
        );
    }

    @Override
    public boolean hasActiveAccounts(Long customerId) {
        return accountRepository.existsByCustomerIdAndAccountStatus(customerId, AccountStatus.ACTIVE);
    }

    @Override
    public boolean validateAccountNumber(String accountNumber) {
        try {
            accountNumberGenerator.validateAccountNumber(accountNumber);
            return accountRepository.existsByAccountNumber(accountNumber);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public AccountResponse getAccountByAccountNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with number: " + accountNumber));

        return accountMapper.toAccountResponse(account, "Account retrieved by account number");
    }

    @Override
    public boolean canCreateAccount(Long customerId, AccountType accountType) {
        return !accountRepository.existsByCustomerIdAndAccountType(customerId, accountType);
    }

    @Override
    public BigDecimal getCustomerTotalBalance(Long customerId) {
        return accountRepository.getTotalBalanceByCustomerId(customerId);
    }

    @Override
    @Transactional
    public List<AccountResponse> bulkUpdateBalances(List<BalanceUpdateRequest> balanceUpdates) {
        log.info("Processing bulk balance updates for {} accounts", balanceUpdates.size());

        validateAdminAccess();

        return balanceUpdates.stream()
                .map(request -> updateAccountBalance(request.getAccountId(), request))
                .collect(Collectors.toList());
    }

    // ==================== VALIDATION OPERATIONS ====================

    @Override
    public void validateAccountAccess(Long accountId) {
        Account account = findAccountById(accountId);
        validateAccountAccess(account);
    }

    @Override
    public void validateBalanceOperation(Long accountId, BigDecimal amount, String operation) {
        Account account = findAccountById(accountId);

        // Check account status allows operations
        if (!account.canCredit() && !account.canDebit()) {
            throw new InvalidAccountStatusException(
                    String.format("Account %s does not allow transactions", account.getAccountNumber())
            );
        }

        // For debit operations, check sufficient balance
        if ("DEBIT".equalsIgnoreCase(operation)) {
            BigDecimal minimumBalance = getMinimumBalance(account.getAccountType());
            BigDecimal balanceAfterDebit = account.getBalance().subtract(amount);

            if (balanceAfterDebit.compareTo(minimumBalance) < 0) {
                throw new InsufficientBalanceException(
                        String.format("Insufficient balance. Available: %.2f, Required: %.2f, Minimum: %.2f",
                                account.getBalance(), amount, minimumBalance)
                );
            }
        }
    }

    @Override
    public BigDecimal getMinimumBalance(AccountType accountType) {
        return switch (accountType) {
            case SAVINGS -> Constants.Account.SAVINGS_MIN_BALANCE;
            case CURRENT -> Constants.Account.CURRENT_MIN_BALANCE;
        };
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private Account findAccountById(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with ID: " + accountId));
    }

    private void validateAccountAccess(Account account) {
        if (!isAdminUser() && !account.getCustomerId().equals(getCurrentUserId())) {
            throw new UnauthorizedAccessException("Access denied to account: " + account.getAccountNumber());
        }
    }

    private void validateCustomerExists(Long customerId) {
        // Call Customer Service to validate customer exists
        try {
            customerServiceClient.getCustomerById(customerId);
        } catch (Exception e) {
            throw new AccountNotFoundException("Customer not found with ID: " + customerId);
        }
    }

    private void validateKycStatus(Long customerId) {
        // Call KYC Service to validate KYC is verified
        try {
            boolean isKycVerified = kycServiceClient.isKycVerified(customerId);
            if (!isKycVerified) {
                throw new InvalidAccountStatusException("KYC not verified for customer: " + customerId);
            }
        } catch (Exception e) {
            log.warn("KYC validation failed for customer: {}, error: {}", customerId, e.getMessage());
            throw new InvalidAccountStatusException("KYC status validation failed for customer: " + customerId);
        }
    }

    private void validateStatusTransition(AccountStatus currentStatus, AccountStatus newStatus) {
        // Define allowed transitions
        boolean isValidTransition = switch (currentStatus) {
            case ACTIVE -> newStatus == AccountStatus.INACTIVE || newStatus == AccountStatus.SUSPENDED ||
                    newStatus == AccountStatus.FROZEN || newStatus == AccountStatus.CLOSED;
            case INACTIVE -> newStatus == AccountStatus.ACTIVE || newStatus == AccountStatus.CLOSED;
            case SUSPENDED -> newStatus == AccountStatus.ACTIVE || newStatus == AccountStatus.CLOSED;
            case FROZEN -> newStatus == AccountStatus.ACTIVE || newStatus == AccountStatus.CLOSED;
            case CLOSED -> false; // No transitions allowed from CLOSED
        };

        if (!isValidTransition) {
            throw new InvalidAccountStatusException(
                    String.format("Invalid status transition from %s to %s", currentStatus, newStatus)
            );
        }
    }

    private void validateCustomerStatusUpdate(AccountStatus currentStatus, AccountStatus newStatus) {
        // Customers can only activate/deactivate their accounts
        boolean isAllowed = (currentStatus == AccountStatus.ACTIVE && newStatus == AccountStatus.INACTIVE) ||
                (currentStatus == AccountStatus.INACTIVE && newStatus == AccountStatus.ACTIVE);

        if (!isAllowed) {
            throw new UnauthorizedAccessException(
                    String.format("Customers cannot change status from %s to %s", currentStatus, newStatus)
            );
        }
    }

    private Account createAccountInternal(Long customerId, AccountType accountType, BigDecimal initialBalance) {
        String accountNumber = accountNumberGenerator.generateAccountNumber(customerId, accountType);

        Account account = Account.builder()
                .customerId(customerId)
                .accountNumber(accountNumber)
                .accountType(accountType)
                .accountStatus(AccountStatus.ACTIVE)
                .balance(initialBalance)
                .build();

        return accountRepository.save(account);
    }

    private boolean isAdminUser() {
        return Constants.Roles.ADMIN.equals(AuthenticatedUser.getRole());
    }

    private Long getCurrentUserId() {
        return AuthenticatedUser.getUserId();
    }

    private void validateAdminAccess() {
        if (!isAdminUser()) {
            throw new UnauthorizedAccessException("Admin access required for this operation");
        }
    }
}