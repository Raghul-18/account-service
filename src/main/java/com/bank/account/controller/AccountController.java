package com.bank.account.controller;

import com.bank.account.client.CustomerServiceClient;
import com.bank.account.dto.AccountCreationRequest;
import com.bank.account.dto.AccountResponse;
import com.bank.account.security.JwtAuthInterceptor;
import com.bank.account.service.AccountService;
import com.bank.account.util.AccountType;
import com.bank.account.util.AuthenticatedUser;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final CustomerServiceClient customerServiceClient;

    /**
     * Create new account for authenticated customer
     * POST /api/accounts/create
     */
    @PostMapping("/create")
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody AccountCreationRequest request,
            HttpServletRequest httpRequest) {

        AuthenticatedUser currentUser = JwtAuthInterceptor.getCurrentUser();
        Long userId = currentUser.getUserId();

        // Resolve userId to customerId
        String jwtToken = httpRequest.getHeader("Authorization");
        Long customerId = customerServiceClient.getCustomerIdByUserId(userId, jwtToken);

        if (customerId == null) {
            throw new RuntimeException("Customer record not found for user: " + userId);
        }

        // Override customerId from JWT (security measure)
        request.setCustomerId(customerId);

        log.info("🏦 User {} creating {} account for customer {}",
                userId, request.getAccountType(), customerId);

        AccountResponse response = accountService.createAccount(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all accounts for authenticated customer
     * GET /api/accounts/my-accounts
     */
    @GetMapping("/my-accounts")
    public ResponseEntity<List<AccountResponse>> getMyAccounts(HttpServletRequest httpRequest) {
        AuthenticatedUser currentUser = JwtAuthInterceptor.getCurrentUser();
        Long userId = currentUser.getUserId();

        // Resolve userId to customerId
        String jwtToken = httpRequest.getHeader("Authorization");
        Long customerId = customerServiceClient.getCustomerIdByUserId(userId, jwtToken);

        if (customerId == null) {
            throw new RuntimeException("Customer record not found for user: " + userId);
        }

        log.info("🔍 User {} fetching accounts for customer {}", userId, customerId);

        List<AccountResponse> accounts = accountService.getAccountsByCustomerId(customerId);
        return ResponseEntity.ok(accounts);
    }

    /**
     * Get specific account details
     * GET /api/accounts/details/{accountId}
     */
    @GetMapping("/details/{accountId}")
    public ResponseEntity<AccountResponse> getAccountDetails(
            @PathVariable Long accountId,
            HttpServletRequest httpRequest) {

        AuthenticatedUser currentUser = JwtAuthInterceptor.getCurrentUser();
        Long userId = currentUser.getUserId();

        // Resolve userId to customerId for ownership verification
        String jwtToken = httpRequest.getHeader("Authorization");
        Long customerId = customerServiceClient.getCustomerIdByUserId(userId, jwtToken);

        if (customerId == null) {
            throw new RuntimeException("Customer record not found for user: " + userId);
        }

        // Verify account ownership
        if (!accountService.verifyAccountOwnership(accountId, customerId)) {
            throw new SecurityException("Access denied - account does not belong to you");
        }

        log.info("🔍 User {} accessing account details for account {}", userId, accountId);

        AccountResponse account = accountService.getAccountById(accountId);
        return ResponseEntity.ok(account);
    }

    /**
     * Get specific account type for customer
     * GET /api/accounts/my-accounts/{accountType}
     */
    @GetMapping("/my-accounts/{accountType}")
    public ResponseEntity<AccountResponse> getMyAccountByType(
            @PathVariable String accountType,
            HttpServletRequest httpRequest) {

        AuthenticatedUser currentUser = JwtAuthInterceptor.getCurrentUser();
        Long userId = currentUser.getUserId();

        // Resolve userId to customerId
        String jwtToken = httpRequest.getHeader("Authorization");
        Long customerId = customerServiceClient.getCustomerIdByUserId(userId, jwtToken);

        if (customerId == null) {
            throw new RuntimeException("Customer record not found for user: " + userId);
        }

        AccountType type = AccountType.fromString(accountType);
        log.info("🔍 User {} fetching {} account for customer {}", userId, type, customerId);

        AccountResponse account = accountService.getAccountByCustomerAndType(customerId, type);
        return ResponseEntity.ok(account);
    }
}