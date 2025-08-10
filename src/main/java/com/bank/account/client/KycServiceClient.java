package com.bank.account.client;

import com.bank.account.exception.InvalidAccountStatusException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Client for communicating with KYC Service
 * Validates KYC status before account operations
 *
 * NOTE: This is a simplified stub implementation for development.
 * Replace with full REST client implementation in production.
 */
@Slf4j
@Component
public class KycServiceClient {

    @Value("${services.kyc.base-url:http://localhost:8084}")
    private String kycServiceBaseUrl;

    /**
     * Check if customer's KYC is verified
     * @param customerId Customer identifier
     * @return true if KYC is verified
     */
    public boolean isKycVerified(Long customerId) {
        log.info("Checking KYC verification status for customer: {}", customerId);

        // TODO: Replace with actual REST call to KYC service
        // For now, assume KYC is verified for valid customer IDs
        if (customerId == null || customerId <= 0) {
            log.warn("Invalid customer ID for KYC check: {}", customerId);
            return false;
        }

        // Simulate KYC verification check
        log.debug("KYC verified for customer: {} (stub implementation)", customerId);
        return true;
    }

    /**
     * Get detailed KYC status information
     * @param customerId Customer identifier
     * @return KYC status details
     */
    public KycStatusDto getKycStatus(Long customerId) {
        log.info("Fetching KYC status for customer: {}", customerId);

        if (customerId == null || customerId <= 0) {
            throw new InvalidAccountStatusException("Invalid customer ID: " + customerId);
        }

        // Simulate successful KYC status retrieval
        return new KycStatusDto(customerId, "VERIFIED", true, "2024-01-15", "All documents verified");
    }

    /**
     * Check if customer can create accounts based on KYC status
     * @param customerId Customer identifier
     * @return true if customer can create accounts
     */
    public boolean canCreateAccounts(Long customerId) {
        try {
            return isKycVerified(customerId);
        } catch (Exception e) {
            log.warn("Cannot determine account creation eligibility for customer {}: {}", customerId, e.getMessage());
            return false;
        }
    }

    /**
     * Validate KYC requirements for account creation
     * @param customerId Customer identifier
     * @throws InvalidAccountStatusException if KYC requirements not met
     */
    public void validateKycForAccountCreation(Long customerId) {
        log.info("Validating KYC for account creation - customer: {}", customerId);

        if (!isKycVerified(customerId)) {
            throw new InvalidAccountStatusException(
                    String.format("KYC not verified for customer %d", customerId)
            );
        }

        log.info("KYC validation successful for customer: {}", customerId);
    }

    /**
     * DTO for KYC status response
     */
    public static class KycStatusDto {
        private Long customerId;
        private String status;
        private boolean allDocumentsSubmitted;
        private String verificationDate;
        private String remarks;

        // Constructors
        public KycStatusDto() {}

        public KycStatusDto(Long customerId, String status, boolean allDocumentsSubmitted,
                            String verificationDate, String remarks) {
            this.customerId = customerId;
            this.status = status;
            this.allDocumentsSubmitted = allDocumentsSubmitted;
            this.verificationDate = verificationDate;
            this.remarks = remarks;
        }

        // Getters and Setters
        public Long getCustomerId() { return customerId; }
        public void setCustomerId(Long customerId) { this.customerId = customerId; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public boolean isAllDocumentsSubmitted() { return allDocumentsSubmitted; }
        public void setAllDocumentsSubmitted(boolean allDocumentsSubmitted) {
            this.allDocumentsSubmitted = allDocumentsSubmitted;
        }

        public String getVerificationDate() { return verificationDate; }
        public void setVerificationDate(String verificationDate) { this.verificationDate = verificationDate; }

        public String getRemarks() { return remarks; }
        public void setRemarks(String remarks) { this.remarks = remarks; }

        // Utility methods
        public boolean isVerified() {
            return "VERIFIED".equalsIgnoreCase(status);
        }

        public boolean isPending() {
            return "PENDING".equalsIgnoreCase(status);
        }

        public boolean isRejected() {
            return "REJECTED".equalsIgnoreCase(status);
        }

        public boolean canCreateAccounts() {
            return isVerified() && allDocumentsSubmitted;
        }

        @Override
        public String toString() {
            return String.format("KycStatus[customerId=%d, status=%s, allDocs=%s, verified=%s]",
                    customerId, status, allDocumentsSubmitted, verificationDate);
        }
    }
}