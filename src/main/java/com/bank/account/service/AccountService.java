package com.bank.account.service;

import com.bank.account.dto.request.*;
import com.bank.account.dto.response.*;
import com.bank.account.entity.AccountStatus;
import com.bank.account.entity.AccountType;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface for Account operations
 * Defines all business operations for account management
 */
public interface AccountService {

    // ==================== CUSTOMER OPERATIONS ====================

    /**
     * Create a new account for a customer
     * Validates customer existence, account type limits, and minimum balance
     *
     * @param request Account creation details
     * @return Created account details
     * @throws DuplicateAccountException if customer already has this account type
     * @throws InvalidBalanceException if initial balance < minimum required
     * @throws UnauthorizedAccessException if customer tries to create for different customer
     */
    AccountResponse createAccount(AccountCreateRequest request);

    /**
     * Get account by ID (customer can only access their own accounts)
     *
     * @param accountId Account identifier
     * @return Account details
     * @throws AccountNotFoundException if account doesn't exist
     * @throws UnauthorizedAccessException if customer tries to access other's account
     */
    AccountResponse getAccountById(Long accountId);

    /**
     * Get all accounts for a specific customer
     * Customers can only see their own accounts, admins can see any customer's accounts
     *
     * @param customerId Customer identifier
     * @return List of customer's accounts with summary
     */
    AccountListResponse getAccountsByCustomerId(Long customerId);

    /**
     * Get account balance and status
     *
     * @param accountId Account identifier
     * @return Balance details with available balance
     * @throws AccountNotFoundException if account doesn't exist
     * @throws UnauthorizedAccessException if unauthorized access
     */
    BalanceResponse getAccountBalance(Long accountId);

    /**
     * Update account status (customer can deactivate/reactivate their own accounts)
     *
     * @param accountId Account identifier
     * @param request Status update details
     * @return Updated account details
     * @throws AccountNotFoundException if account doesn't exist
     * @throws InvalidAccountStatusException if status transition not allowed
     * @throws UnauthorizedAccessException if unauthorized access
     */
    AccountResponse updateAccountStatus(Long accountId, StatusUpdateRequest request);

    /**
     * Get customer's account summary (overview of all accounts)
     *
     * @param customerId Customer identifier
     * @return Summary with total balance, account count, etc.
     */
    CustomerAccountSummaryResponse getCustomerAccountSummary(Long customerId);

    // ==================== ADMIN OPERATIONS ====================

    /**
     * Get all accounts in the system (Admin only)
     * Supports filtering and pagination
     *
     * @param filterRequest Filter criteria and pagination
     * @return Filtered list of accounts
     */
    AccountListResponse getAllAccounts(AccountFilterRequest filterRequest);

    /**
     * Update account balance (Admin only)
     * Used for admin adjustments, interest credits, penalty debits
     *
     * @param accountId Account identifier
     * @param request Balance update details
     * @return Updated account details
     * @throws AccountNotFoundException if account doesn't exist
     * @throws InvalidBalanceException if new balance invalid
     * @throws InvalidAccountStatusException if account closed/frozen
     */
    AccountResponse updateAccountBalance(Long accountId, BalanceUpdateRequest request);

    /**
     * Force update account status (Admin only)
     * Admins can perform any status transition including FREEZE, SUSPEND, CLOSE
     *
     * @param accountId Account identifier
     * @param request Status update details
     * @return Updated account details
     * @throws AccountNotFoundException if account doesn't exist
     */
    AccountResponse forceUpdateAccountStatus(Long accountId, StatusUpdateRequest request);

    /**
     * Get comprehensive account statistics (Admin only)
     * Dashboard data with counts, balances, trends
     *
     * @return System-wide account statistics
     */
    AccountStatsResponse getAccountStatistics();

    /**
     * Get accounts filtered by status (Admin only)
     *
     * @param status Account status filter
     * @return List of accounts with given status
     */
    List<AccountResponse> getAccountsByStatus(AccountStatus status);

    /**
     * Get accounts filtered by type (Admin only)
     *
     * @param accountType Account type filter
     * @return List of accounts with given type
     */
    List<AccountResponse> getAccountsByType(AccountType accountType);

    /**
     * Permanently delete an account (Admin only)
     * Only allowed if balance is zero and account is closed
     *
     * @param accountId Account identifier
     * @throws AccountNotFoundException if account doesn't exist
     * @throws InvalidBalanceException if balance is not zero
     * @throws InvalidAccountStatusException if account is not closed
     */
    void deleteAccount(Long accountId);

    // ==================== INTERNAL/INTER-SERVICE OPERATIONS ====================

    /**
     * Create account automatically after KYC verification (Kafka event)
     * Creates both SAVINGS and CURRENT accounts with minimum balances
     *
     * @param customerId Customer who got KYC verified
     * @return List of created accounts
     */
    List<AccountResponse> createAccountsAfterKycVerification(Long customerId);

    /**
     * Check if customer has any active accounts (Internal use)
     *
     * @param customerId Customer identifier
     * @return true if customer has active accounts
     */
    boolean hasActiveAccounts(Long customerId);

    /**
     * Validate account number format and existence (Internal use)
     *
     * @param accountNumber Account number to validate
     * @return true if valid and exists
     */
    boolean validateAccountNumber(String accountNumber);

    /**
     * Get account by account number (Internal use)
     * Used by other services for validation
     *
     * @param accountNumber Account number
     * @return Account details
     * @throws AccountNotFoundException if account doesn't exist
     */
    AccountResponse getAccountByAccountNumber(String accountNumber);

    /**
     * Check if customer can have more accounts of given type
     *
     * @param customerId Customer identifier
     * @param accountType Account type to check
     * @return true if customer can have more accounts of this type
     */
    boolean canCreateAccount(Long customerId, AccountType accountType);

    /**
     * Get total customer balance across all accounts
     *
     * @param customerId Customer identifier
     * @return Total balance across all customer accounts
     */
    BigDecimal getCustomerTotalBalance(Long customerId);

    /**
     * Bulk balance update for multiple accounts (Internal use)
     * Used for batch operations like interest calculations
     *
     * @param balanceUpdates List of balance updates
     * @return List of updated accounts
     */
    List<AccountResponse> bulkUpdateBalances(List<BalanceUpdateRequest> balanceUpdates);

    // ==================== VALIDATION OPERATIONS ====================

    /**
     * Validate account exists and user has access
     *
     * @param accountId Account identifier
     * @throws AccountNotFoundException if account doesn't exist
     * @throws UnauthorizedAccessException if no access
     */
    void validateAccountAccess(Long accountId);

    /**
     * Validate balance operation is allowed
     *
     * @param accountId Account identifier
     * @param amount Amount for operation
     * @param operation Type of operation (DEBIT/CREDIT)
     * @throws AccountNotFoundException if account doesn't exist
     * @throws InsufficientBalanceException if insufficient balance
     * @throws InvalidAccountStatusException if account status doesn't allow operation
     */
    void validateBalanceOperation(Long accountId, BigDecimal amount, String operation);

    /**
     * Get account minimum balance based on type
     *
     * @param accountType Account type
     * @return Minimum balance required
     */
    BigDecimal getMinimumBalance(AccountType accountType);
}