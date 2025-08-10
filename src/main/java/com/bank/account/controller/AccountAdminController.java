package com.bank.account.controller;

import com.bank.account.dto.AccountResponse;
import com.bank.account.dto.AccountStatsResponse;
import com.bank.account.dto.AccountStatusUpdateRequest;
import com.bank.account.dto.BalanceUpdateRequest;
import com.bank.account.security.JwtAuthInterceptor;
import com.bank.account.service.AccountService;
import com.bank.account.util.AuthenticatedUser;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/accounts/admin")
@RequiredArgsConstructor
public class AccountAdminController {

    private final AccountService accountService;

    /**
     * Get all accounts (Admin only)
     * GET /api/accounts/admin/all
     */
    @GetMapping("/all")
    public ResponseEntity<List<AccountResponse>> getAllAccounts() {
        validateAdmin();

        log.info("👑 Admin fetching all accounts");
        List<AccountResponse> accounts = accountService.getAllAccounts();
        return ResponseEntity.ok(accounts);
    }

    /**
     * Get accounts by customer ID (Admin only)
     * GET /api/accounts/admin/customer/{customerId}
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<AccountResponse>> getAccountsByCustomer(@PathVariable Long customerId) {
        validateAdmin();

        log.info("👑 Admin fetching accounts for customer {}", customerId);
        List<AccountResponse> accounts = accountService.getAccountsByCustomerId(customerId);
        return ResponseEntity.ok(accounts);
    }

    /**
     * Get account by ID (Admin only)
     * GET /api/accounts/admin/{accountId}
     */
    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable Long accountId) {
        validateAdmin();

        log.info("👑 Admin fetching account {}", accountId);
        AccountResponse account = accountService.getAccountById(accountId);
        return ResponseEntity.ok(account);
    }

    /**
     * Update account status (Admin only)
     * PUT /api/accounts/admin/{accountId}/status
     */
    @PutMapping("/{accountId}/status")
    public ResponseEntity<AccountResponse> updateAccountStatus(
            @PathVariable Long accountId,
            @Valid @RequestBody AccountStatusUpdateRequest request) {

        validateAdmin();
        AuthenticatedUser currentUser = JwtAuthInterceptor.getCurrentUser();

        log.info("👑 Admin {} updating account {} status to {}",
                currentUser.getUsername(), accountId, request.getAccountStatus());

        AccountResponse response = accountService.updateAccountStatus(accountId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Update account balance (Admin only)
     * PUT /api/accounts/admin/{accountId}/balance
     */
    @PutMapping("/{accountId}/balance")
    public ResponseEntity<AccountResponse> updateAccountBalance(
            @PathVariable Long accountId,
            @Valid @RequestBody BalanceUpdateRequest request) {

        validateAdmin();
        AuthenticatedUser currentUser = JwtAuthInterceptor.getCurrentUser();

        log.info("👑 Admin {} updating account {} balance to ₹{}",
                currentUser.getUsername(), accountId, request.getBalance());

        AccountResponse response = accountService.updateAccountBalance(accountId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get accounts by status (Admin only)
     * GET /api/accounts/admin/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<AccountResponse>> getAccountsByStatus(@PathVariable String status) {
        validateAdmin();

        log.info("👑 Admin fetching accounts with status: {}", status);
        List<AccountResponse> accounts = accountService.getAccountsByStatus(status);
        return ResponseEntity.ok(accounts);
    }

    /**
     * Get account statistics (Admin only)
     * GET /api/accounts/admin/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<AccountStatsResponse> getAccountStats() {
        validateAdmin();

        log.info("👑 Admin fetching account statistics");
        AccountStatsResponse stats = accountService.getAccountStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * Create accounts for a customer (Admin only - manual trigger)
     * POST /api/accounts/admin/create-for-customer/{customerId}
     */
    @PostMapping("/create-for-customer/{customerId}")
    public ResponseEntity<List<AccountResponse>> createAccountsForCustomer(@PathVariable Long customerId) {
        validateAdmin();
        AuthenticatedUser currentUser = JwtAuthInterceptor.getCurrentUser();

        log.info("👑 Admin {} manually creating accounts for customer {}",
                currentUser.getUsername(), customerId);

        // Create accounts automatically
        accountService.createAccountsForKycCompletedCustomer(customerId);

        // Return the created accounts
        List<AccountResponse> accounts = accountService.getAccountsByCustomerId(customerId);
        return ResponseEntity.ok(accounts);
    }

    /**
     * Helper method to validate admin access
     */
    private void validateAdmin() {
        AuthenticatedUser currentUser = JwtAuthInterceptor.getCurrentUser();
        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            throw new SecurityException("Only ADMIN can access this endpoint");
        }
    }
}