package com.bank.account.client;

import com.bank.account.exception.AccountNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Client for communicating with Customer Service
 * Validates customer existence before account operations
 *
 * NOTE: This is a simplified stub implementation for development.
 * Replace with full REST client implementation in production.
 */
@Slf4j
@Component
public class CustomerServiceClient {

    @Value("${services.customer.base-url:http://localhost:8081}")
    private String customerServiceBaseUrl;

    /**
     * Get customer details by ID
     * @param customerId Customer identifier
     * @throws AccountNotFoundException if customer doesn't exist
     */
    public CustomerDto getCustomerById(Long customerId) {
        log.info("Validating customer existence: {}", customerId);

        // TODO: Replace with actual REST call to customer service
        // For now, assume customer exists if ID is valid
        if (customerId == null || customerId <= 0) {
            throw new AccountNotFoundException("Invalid customer ID: " + customerId);
        }

        // Simulate successful customer validation
        log.debug("Customer {} validated successfully (stub implementation)", customerId);

        return new CustomerDto(customerId, "John", "Doe", "john.doe@example.com", "ACTIVE");
    }

    /**
     * Check if customer exists (lightweight check)
     * @param customerId Customer identifier
     * @return true if customer exists
     */
    public boolean customerExists(Long customerId) {
        try {
            getCustomerById(customerId);
            return true;
        } catch (AccountNotFoundException e) {
            return false;
        }
    }

    /**
     * Simple DTO for customer validation response
     */
    public static class CustomerDto {
        private Long customerId;
        private String firstName;
        private String lastName;
        private String email;
        private String status;

        // Constructors
        public CustomerDto() {}

        public CustomerDto(Long customerId, String firstName, String lastName, String email, String status) {
            this.customerId = customerId;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.status = status;
        }

        // Getters and Setters
        public Long getCustomerId() { return customerId; }
        public void setCustomerId(Long customerId) { this.customerId = customerId; }

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public boolean isActive() {
            return "ACTIVE".equalsIgnoreCase(status);
        }

        @Override
        public String toString() {
            return String.format("Customer[id=%d, name=%s %s, status=%s]",
                    customerId, firstName, lastName, status);
        }
    }
}