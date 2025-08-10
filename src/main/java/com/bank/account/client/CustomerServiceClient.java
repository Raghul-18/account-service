package com.bank.account.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class CustomerServiceClient {

    @Value("${services.customer.base-url:http://localhost:8081}")
    private String customerServiceBaseUrl;

    private final RestTemplate restTemplate;

    public CustomerServiceClient() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Get customerId for a given userId by calling Customer Service
     */
    public Long getCustomerIdByUserId(Long userId, String jwtToken) {
        try {
            String url = customerServiceBaseUrl + "/api/customers/user/" + userId + "/customer-id";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(jwtToken.replace("Bearer ", ""));
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            log.info("🔍 Calling Customer Service: GET {}", url);

            ResponseEntity<CustomerIdResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, CustomerIdResponse.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Long customerId = response.getBody().getCustomerId();
                log.info("✅ Resolved userId {} to customerId {}", userId, customerId);
                return customerId;
            }

            log.warn("❌ Customer Service returned empty response for userId {}", userId);
            return null;

        } catch (Exception e) {
            log.error("❌ Failed to resolve userId {} to customerId: {}", userId, e.getMessage());
            throw new RuntimeException("Unable to resolve customer for user: " + userId, e);
        }
    }

    /**
     * Verify that a customerId belongs to a specific userId
     */
    public boolean verifyCustomerOwnership(Long customerId, Long userId, String jwtToken) {
        try {
            String url = customerServiceBaseUrl + "/api/customers/" + customerId + "/verify-ownership/" + userId;

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(jwtToken.replace("Bearer ", ""));
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Boolean> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, Boolean.class);

            return response.getStatusCode() == HttpStatus.OK &&
                    Boolean.TRUE.equals(response.getBody());

        } catch (Exception e) {
            log.error("❌ Failed to verify ownership customerId {} for userId {}: {}",
                    customerId, userId, e.getMessage());
            return false;
        }
    }

    /**
     * Check if customer exists and has verified KYC status
     */
    public boolean isCustomerKycVerified(Long customerId, String jwtToken) {
        try {
            String url = customerServiceBaseUrl + "/api/customers/" + customerId + "/status";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(jwtToken.replace("Bearer ", ""));
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<CustomerResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, CustomerResponse.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String kycStatus = response.getBody().getKycStatus();
                boolean isVerified = "VERIFIED".equalsIgnoreCase(kycStatus);
                log.info("📋 Customer {} KYC status: {} (verified: {})", customerId, kycStatus, isVerified);
                return isVerified;
            }

            return false;

        } catch (Exception e) {
            log.error("❌ Failed to check KYC status for customerId {}: {}", customerId, e.getMessage());
            return false;
        }
    }

    // Response DTOs for Customer Service calls
    public static class CustomerIdResponse {
        private Long customerId;

        public CustomerIdResponse() {}

        public CustomerIdResponse(Long customerId) {
            this.customerId = customerId;
        }

        public Long getCustomerId() { return customerId; }
        public void setCustomerId(Long customerId) { this.customerId = customerId; }
    }

    public static class CustomerResponse {
        private Long customerId;
        private String fullName;
        private String kycStatus;
        private String message;

        // Constructors
        public CustomerResponse() {}

        // Getters and setters
        public Long getCustomerId() { return customerId; }
        public void setCustomerId(Long customerId) { this.customerId = customerId; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getKycStatus() { return kycStatus; }
        public void setKycStatus(String kycStatus) { this.kycStatus = kycStatus; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}