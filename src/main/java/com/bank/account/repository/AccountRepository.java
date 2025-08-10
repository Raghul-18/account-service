// src/main/java/com/bank/account/repository/AccountRepository.java
package com.bank.account.repository;

import com.bank.account.entity.Account;
import com.bank.account.entity.AccountStatus;
import com.bank.account.util.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    // Find accounts by customer ID
    List<Account> findByCustomerId(Long customerId);

    // Find account by account number
    Optional<Account> findByAccountNumber(String accountNumber);

    // Check if customer already has an account of specific type
    boolean existsByCustomerIdAndAccountType(Long customerId, AccountType accountType);

    // Find account by customer and type
    Optional<Account> findByCustomerIdAndAccountType(Long customerId, AccountType accountType);

    // Count accounts by type (for account number generation)
    long countByAccountType(AccountType accountType);

    // Find accounts by status
    List<Account> findByAccountStatus(AccountStatus status);

    // Find active accounts by customer
    List<Account> findByCustomerIdAndAccountStatus(Long customerId, AccountStatus status);

    // Custom queries for admin operations
    @Query("SELECT COUNT(a) FROM Account a WHERE a.accountStatus = :status")
    long countByAccountStatus(@Param("status") AccountStatus status);

    @Query("SELECT SUM(a.balance) FROM Account a WHERE a.accountStatus = 'ACTIVE'")
    BigDecimal getTotalActiveBalance();

    @Query("SELECT a.accountType, COUNT(a) FROM Account a GROUP BY a.accountType")
    List<Object[]> getAccountCountByType();

    @Query("SELECT a.accountStatus, COUNT(a) FROM Account a GROUP BY a.accountStatus")
    List<Object[]> getAccountCountByStatus();

    // Find accounts with balance above threshold
    List<Account> findByBalanceGreaterThan(BigDecimal threshold);

    // Find accounts with balance below threshold
    List<Account> findByBalanceLessThan(BigDecimal threshold);

    // Find accounts created in date range
    @Query("SELECT a FROM Account a WHERE a.createdAt >= :startDate AND a.createdAt <= :endDate ORDER BY a.createdAt DESC")
    List<Account> findAccountsCreatedBetween(
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate
    );

    // Check if account belongs to customer (for access control)
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Account a WHERE a.accountId = :accountId AND a.customerId = :customerId")
    boolean existsByAccountIdAndCustomerId(@Param("accountId") Long accountId, @Param("customerId") Long customerId);
}