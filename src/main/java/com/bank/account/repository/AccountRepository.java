package com.bank.account.repository;

import com.bank.account.entity.Account;
import com.bank.account.entity.AccountStatus;
import com.bank.account.entity.AccountType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    // Verify your AccountRepository has ALL these methods:

    // Basic CRUD operations
    Optional<Account> findById(Long accountId);
    List<Account> findByCustomerId(Long customerId);
    List<Account> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    Optional<Account> findByAccountNumber(String accountNumber);

    // Existence checks
    boolean existsByAccountNumber(String accountNumber);
    boolean existsByCustomerIdAndAccountType(Long customerId, AccountType accountType);
    boolean existsByCustomerIdAndAccountStatus(Long customerId, AccountStatus status);

    // Status and type queries
    List<Account> findByAccountStatus(AccountStatus status);
    List<Account> findByAccountType(AccountType accountType);

    // NEW: Pagination methods (ADD THESE IF MISSING)
    Page<Account> findByAccountStatus(AccountStatus status, Pageable pageable);
    Page<Account> findByAccountType(AccountType accountType, Pageable pageable);
    Page<Account> findByAccountStatusAndAccountType(AccountStatus status, AccountType accountType, Pageable pageable);

    // NEW: Statistics methods (ADD THESE IF MISSING)
    @Query("SELECT COALESCE(SUM(a.balance), 0) FROM Account a")
    BigDecimal getTotalBalance();

    @Query("SELECT COUNT(DISTINCT a.customerId) FROM Account a")
    Long getUniqueCustomerCount();

    @Query("SELECT COALESCE(AVG(a.balance), 0) FROM Account a")
    BigDecimal getAverageBalance();

    @Query("SELECT COALESCE(AVG(customerBalance.totalBalance), 0) FROM " +
            "(SELECT SUM(a.balance) as totalBalance FROM Account a GROUP BY a.customerId) customerBalance")
    BigDecimal getAverageBalancePerCustomer();

    Long countByAccountStatus(AccountStatus status);
    Long countByAccountType(AccountType accountType);

    @Query("SELECT COALESCE(SUM(a.balance), 0) FROM Account a WHERE a.customerId = :customerId")
    BigDecimal getTotalBalanceByCustomerId(@Param("customerId") Long customerId);
}