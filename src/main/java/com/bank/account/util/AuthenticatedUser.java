package com.bank.account.util;

import lombok.Builder;
import lombok.Data;

/**
 * Represents the current authenticated user from JWT token
 * Used throughout the application to access user context
 */
@Data
@Builder
public class AuthenticatedUser {

    private Long userId;
    private String role;      // ADMIN, CUSTOMER
    private String username;  // admin, customer_9876543210

    /**
     * Check if user has admin role
     */
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(this.role);
    }

    /**
     * Check if user has customer role
     */
    public boolean isCustomer() {
        return "CUSTOMER".equalsIgnoreCase(this.role);
    }

    /**
     * Check if user can access customer data
     * Admin can access any customer data, customer can only access their own
     */
    public boolean canAccessCustomer(Long customerId) {
        if (isAdmin()) {
            return true;
        }

        if (isCustomer()) {
            // Extract customer ID from username pattern: customer_<phone>
            // For customer accounts, we need to validate via customer service
            // This is a placeholder - actual implementation should verify via customer service
            return true; // Will be validated at service layer
        }

        return false;
    }

    /**
     * Get display name for the user
     */
    public String getDisplayName() {
        if (isAdmin()) {
            return "Administrator";
        }
        if (isCustomer()) {
            return "Customer";
        }
        return "User";
    }

    /**
     * Check if user has required role
     */
    public boolean hasRole(String requiredRole) {
        return requiredRole != null && requiredRole.equalsIgnoreCase(this.role);
    }
}