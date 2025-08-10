package com.bank.account.repository;

import com.bank.account.entity.Account;
import com.bank.account.entity.AccountStatus;
import com.bank.account.entity.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Account entity
 * Provides data access methods for account operations
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    // ===== BASIC FINDERS =====

    /**
     * Find account by account number
     */
    Optional<Account> findByAccountNumber(String accountNumber);

    /**
     * Find all accounts for a specific customer
     */
    List<Account> findByCustomerId(Long customerId);

    /**
     * Find account by customer ID and account type
     * (Used to enforce one account per type per customer)
     */
    Optional<Account> findByCustomerIdAndAccountType(Long customerId, AccountType accountType);

    /**
     * Find all accounts by status
     */
    List<Account> findByAccountStatus(AccountStatus accountStatus);

    /**
     * Find all active accounts for a customer
     */
    List<Account> findByCustomerIdAndAccountStatus(Long customerId, AccountStatus accountStatus);

    // ===== EXISTENCE CHECKS =====

    /**
     * Check if customer already has an account of specific type
     */
    boolean existsByCustomerIdAndAccountType(Long customerId, AccountType accountType);

    /**
     * Check if account number already exists
     */
    boolean existsByAccountNumber(String accountNumber);

    /**
     * Check if customer has any accounts
     */
    boolean existsByCustomerId(Long customerId);

    // ===== BALANCE QUERIES =====

    /**
     * Find accounts with balance greater than specified amount
     */
    @Query("SELECT a FROM Account a WHERE a.balance > :minBalance")
    List<Account> findAccountsWithBalanceGreaterThan(@Param("minBalance") BigDecimal minBalance);

    /**
     * Find accounts with balance less than minimum required for their type
     */
    @Query("SELECT a FROM Account a WHERE " +
            "CASE " +
            "WHEN a.accountType = 'SAVINGS' THEN a.balance < :savingsMinBalance " +
            "WHEN a.accountType = 'CURRENT' THEN a.balance < :currentMinBalance " +
            "ELSE false END")
    List<Account> findAccountsBelowMinimumBalance(
            @Param("savingsMinBalance") BigDecimal savingsMinBalance,
            @Param("currentMinBalance") BigDecimal currentMinBalance);

    /**
     * Get total balance for a customer across all accounts
     */
    @Query("SELECT COALESCE(SUM(a.balance), 0) FROM Account a WHERE a.customerId = :customerId")
    BigDecimal getTotalBalanceByCustomerId(@Param("customerId") Long customerId);

    // ===== ADMIN QUERIES =====

    /**
     * Find all accounts created within a date range
     */
    @Query("SELECT a FROM Account a WHERE a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    List<Account> findAccountsCreatedBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Count accounts by status
     */
    @Query("SELECT a.accountStatus, COUNT(a) FROM Account a GROUP BY a.accountStatus")
    List<Object[]> countAccountsByStatus();

    /**
     * Count accounts by type
     */
    @Query("SELECT a.accountType, COUNT(a) FROM Account a GROUP BY a.accountType")
    List<Object[]> countAccountsByType();

    /**
     * Find dormant accounts (no recent activity - based on updated_at)
     */
    @Query("SELECT a FROM Account a WHERE a.updatedAt < :cutoffDate AND a.accountStatus = 'ACTIVE'")
    List<Account> findDormantAccounts(@Param("cutoffDate") LocalDateTime cutoffDate);

    // ===== STATISTICS QUERIES =====

    /**
     * Get account statistics summary
     */
    @Query("SELECT " +
            "COUNT(a) as totalAccounts, " +
            "SUM(CASE WHEN a.accountStatus = 'ACTIVE' THEN 1 ELSE 0 END) as activeAccounts, " +
            "SUM(CASE WHEN a.accountType = 'SAVINGS' THEN 1 ELSE 0 END) as savingsAccounts, " +
            "SUM(CASE WHEN a.accountType = 'CURRENT' THEN 1 ELSE 0 END) as currentAccounts, " +
            "COALESCE(SUM(a.balance), 0) as totalBalance, " +
            "COALESCE(AVG(a.balance), 0) as averageBalance " +
            "FROM Account a")
    Object[] getAccountStatistics();

    /**
     * Get customer account summary (for customer dashboard)
     */
    @Query("SELECT " +
            "a.accountType, " +
            "a.accountNumber, " +
            "a.accountStatus, " +
            "a.balance, " +
            "a.createdAt " +
            "FROM Account a WHERE a.customerId = :customerId ORDER BY a.createdAt")
    List<Object[]> getCustomerAccountSummary(@Param("customerId") Long customerId);

    // ===== CUSTOM BUSINESS QUERIES =====

    /**
     * Find accounts eligible for interest calculation
     * (Active savings accounts with positive balance)
     */
    @Query("SELECT a FROM Account a WHERE " +
            "a.accountType = 'SAVINGS' AND " +
            "a.accountStatus = 'ACTIVE' AND " +
            "a.balance > 0")
    List<Account> findSavingsAccountsForInterest();

    /**
     * Find accounts that need minimum balance warning
     */
    @Query("SELECT a FROM Account a WHERE " +
            "a.accountStatus = 'ACTIVE' AND " +
            "((a.accountType = 'SAVINGS' AND a.balance < :savingsMinBalance) OR " +
            "(a.accountType = 'CURRENT' AND a.balance < :currentMinBalance))")
    List<Account> findAccountsNeedingMinBalanceWarning(
            @Param("savingsMinBalance") BigDecimal savingsMinBalance,
            @Param("currentMinBalance") BigDecimal currentMinBalance);

    /**
     * Find recently created accounts (for monitoring/reporting)
     */
    @Query("SELECT a FROM Account a WHERE a.createdAt >= :sinceDate ORDER BY a.createdAt DESC")
    List<Account> findRecentlyCreatedAccounts(@Param("sinceDate") LocalDateTime sinceDate);

    /**
     * Find accounts by customer IDs (bulk lookup)
     */
    @Query("SELECT a FROM Account a WHERE a.customerId IN :customerIds ORDER BY a.customerId, a.accountType")
    List<Account> findByCustomerIds(@Param("customerIds") List<Long> customerIds);

    // ===== ACCOUNT NUMBER GENERATION SUPPORT =====

    /**
     * Find the highest account number for a customer (for generating next sequence)
     */
    @Query("SELECT MAX(a.accountNumber) FROM Account a WHERE a.customerId = :customerId")
    Optional<String> findMaxAccountNumberForCustomer(@Param("customerId") Long customerId);

    /**
     * Find accounts with account numbers starting with a prefix
     */
    @Query("SELECT a FROM Account a WHERE a.accountNumber LIKE :prefix% ORDER BY a.accountNumber")
    List<Account> findByAccountNumberStartingWith(@Param("prefix") String prefix);

    // ===== VALIDATION QUERIES =====

    /**
     * Validate if customer can create new account of given type
     */
    @Query("SELECT CASE WHEN COUNT(a) = 0 THEN true ELSE false END " +
            "FROM Account a WHERE a.customerId = :customerId AND a.accountType = :accountType")
    boolean canCreateAccountOfType(@Param("customerId") Long customerId, @Param("accountType") AccountType accountType);

    /**
     * Count active accounts for a customer
     */
    @Query("SELECT COUNT(a) FROM Account a WHERE a.customerId = :customerId AND a.accountStatus = 'ACTIVE'")
    long countActiveAccountsByCustomerId(@Param("customerId") Long customerId);

    /**
     * Find customer's primary account (first created account or savings if available)
     */
    @Query("SELECT a FROM Account a WHERE a.customerId = :customerId AND a.accountStatus = 'ACTIVE' " +
            "ORDER BY CASE WHEN a.accountType = 'SAVINGS' THEN 1 ELSE 2 END, a.createdAt ASC")
    List<Account> findCustomerPrimaryAccount(@Param("customerId") Long customerId);
}