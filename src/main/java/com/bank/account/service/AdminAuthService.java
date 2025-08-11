package com.bank.account.service;

public interface AdminAuthService {
    /**
     * Returns a valid admin JWT token. Handles caching and refresh.
     */
    String getAdminJwt();
}
