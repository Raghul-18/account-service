package com.bank.account.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a bank account
 * Each customer can have maximum one SAVINGS and one CURRENT account
 */
@Entity
@Table(name = "ACCOUNTS",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_customer_account_type",
                        columnNames = {"customer_id", "account_type"}),
                @UniqueConstraint(name = "uk_account_number",
                        columnNames = {"account_number"})
        },
        indexes = {
                @Index(name = "idx_accounts_customer_id", columnList = "customer_id"),
                @Index(name = "idx_accounts_status", columnList = "account_status"),
                @Index(name = "idx_accounts_created_at", columnList = "created_at")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"balance"}) // Exclude sensitive data from toString
@EqualsAndHashCode(of = {"accountId", "accountNumber"}) // Use immutable fields for equals/hashCode
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long accountId;

    @NotNull(message = "Customer ID is required")
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @NotBlank(message = "Account number is required")
    @Size(min = 10, max = 20, message = "Account number must be between 10 and 20 characters")
    @Column(name = "account_number", nullable = false, unique = true, length = 20)
    private String accountNumber;

    @NotNull(message = "Account type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20)
    private AccountType accountType;

    @NotNull(message = "Account status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false, length = 20)
    @Builder.Default
    private AccountStatus accountStatus = AccountStatus.ACTIVE;

    @NotNull(message = "Balance is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Balance cannot be negative")
    @Digits(integer = 13, fraction = 2, message = "Invalid balance format")
    @Column(name = "balance", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Business logic methods

    /**
     * Check if account is active and can perform transactions
     */
    public boolean isActive() {
        return AccountStatus.ACTIVE.equals(this.accountStatus);
    }

    /**
     * Check if account allows balance inquiry
     */
    public boolean allowsBalanceInquiry() {
        return this.accountStatus != null && this.accountStatus.allowsBalanceInquiry();
    }

    /**
     * Check if account allows transactions
     */
    public boolean allowsTransactions() {
        return this.accountStatus != null && this.accountStatus.allowsTransactions();
    }

    /**
     * Check if balance meets minimum requirement for the account type
     */
    public boolean hasMinimumBalance() {
        if (accountType == null || balance == null) {
            return false;
        }
        return balance.compareTo(BigDecimal.valueOf(accountType.getMinimumBalance())) >= 0;
    }

    /**
     * Get available balance (current balance)
     * In future, this could subtract holds/frozen amounts
     */
    public BigDecimal getAvailableBalance() {
        return balance;
    }

    /**
     * Validate if the account can be debited with the given amount
     */
    public boolean canDebit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        if (!allowsTransactions()) {
            return false;
        }

        BigDecimal newBalance = balance.subtract(amount);
        return newBalance.compareTo(BigDecimal.valueOf(accountType.getMinimumBalance())) >= 0;
    }

    /**
     * Validate if the account can be credited with the given amount
     */
    public boolean canCredit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        return allowsTransactions();
    }

    /**
     * Update balance (use with caution - should be done through service layer)
     */
    public void updateBalance(BigDecimal newBalance) {
        if (newBalance != null && newBalance.compareTo(BigDecimal.ZERO) >= 0) {
            this.balance = newBalance;
        }
    }

    /**
     * Add amount to balance
     */
    public void credit(BigDecimal amount) {
        if (canCredit(amount)) {
            this.balance = this.balance.add(amount);
        } else {
            throw new IllegalStateException("Cannot credit amount: " + amount + " to account: " + accountNumber);
        }
    }

    /**
     * Subtract amount from balance
     */
    public void debit(BigDecimal amount) {
        if (canDebit(amount)) {
            this.balance = this.balance.subtract(amount);
        } else {
            throw new IllegalStateException("Cannot debit amount: " + amount + " from account: " + accountNumber);
        }
    }

    /**
     * Activate the account
     */
    public void activate() {
        if (this.accountStatus == AccountStatus.CLOSED) {
            throw new IllegalStateException("Cannot activate a closed account");
        }
        this.accountStatus = AccountStatus.ACTIVE;
    }

    /**
     * Deactivate the account
     */
    public void deactivate() {
        if (this.accountStatus != AccountStatus.CLOSED) {
            this.accountStatus = AccountStatus.INACTIVE;
        }
    }

    /**
     * Close the account permanently
     */
    public void close() {
        if (balance.compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalStateException("Cannot close account with positive balance");
        }
        this.accountStatus = AccountStatus.CLOSED;
    }

    /**
     * Get account type display name
     */
    public String getAccountTypeDisplay() {
        return accountType != null ? accountType.getDisplayName() : null;
    }

    /**
     * Get account status display name
     */
    public String getAccountStatusDisplay() {
        return accountStatus != null ? accountStatus.getDisplayName() : null;
    }

    // Lifecycle callbacks for auditing

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (balance == null) {
            balance = BigDecimal.ZERO;
        }
        if (accountStatus == null) {
            accountStatus = AccountStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}