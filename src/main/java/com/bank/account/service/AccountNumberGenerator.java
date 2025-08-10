package com.bank.account.service;

import com.bank.account.entity.AccountType;
import com.bank.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Service for generating unique account numbers
 *
 * Format: BANK{customerId}{accountTypeCode}{sequence}
 * Example: BANK123SAV001, BANK123CUR001
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountNumberGenerator {

    private final AccountRepository accountRepository;

    @Value("${account.number.prefix:BANK}")
    private String accountNumberPrefix;

    @Value("${account.number.sequence.length:3}")
    private int sequenceLength;

    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generate unique account number for a customer and account type
     *
     * @param customerId Customer ID
     * @param accountType Account type (SAVINGS/CURRENT)
     * @return Unique account number
     */
    public String generateAccountNumber(Long customerId, AccountType accountType) {
        log.debug("🔢 Generating account number for customer {} and type {}", customerId, accountType);

        // Validate inputs
        if (customerId == null || accountType == null) {
            throw new IllegalArgumentException("Customer ID and account type are required");
        }

        String baseNumber = buildBaseAccountNumber(customerId, accountType);
        String uniqueAccountNumber = ensureUniqueness(baseNumber);

        log.info("✅ Generated account number: {} for customer {}", uniqueAccountNumber, customerId);
        return uniqueAccountNumber;
    }

    /**
     * Build base account number without ensuring uniqueness
     */
    private String buildBaseAccountNumber(Long customerId, AccountType accountType) {
        // Format: BANK + customerId + accountTypeCode + sequence
        String typeCode = accountType.getCode(); // SAV or CUR
        String sequence = generateSequence();

        return String.format("%s%d%s%s",
                accountNumberPrefix,
                customerId,
                typeCode,
                sequence);
    }

    /**
     * Generate sequence part of account number
     */
    private String generateSequence() {
        // Generate random sequence number
        int maxSequence = (int) Math.pow(10, sequenceLength) - 1;
        int sequence = ThreadLocalRandom.current().nextInt(1, maxSequence + 1);

        // Format with leading zeros
        String format = "%0" + sequenceLength + "d";
        return String.format(format, sequence);
    }

    /**
     * Ensure the generated account number is unique
     * If collision occurs, regenerate until unique
     */
    private String ensureUniqueness(String baseAccountNumber) {
        String accountNumber = baseAccountNumber;
        int attempts = 0;
        final int maxAttempts = 100; // Prevent infinite loop

        while (attempts < maxAttempts) {
            if (!accountRepository.existsByAccountNumber(accountNumber)) {
                return accountNumber;
            }

            log.warn("⚠️ Account number collision detected: {}. Regenerating...", accountNumber);
            attempts++;

            // Regenerate with different sequence
            accountNumber = regenerateWithNewSequence(baseAccountNumber);
        }

        throw new RuntimeException("Failed to generate unique account number after " + maxAttempts + " attempts");
    }

    /**
     * Regenerate account number with new sequence when collision occurs
     */
    private String regenerateWithNewSequence(String originalNumber) {
        // Extract customer ID and type code from original number
        // BANK123SAV001 -> extract 123 and SAV, generate new sequence

        String withoutPrefix = originalNumber.substring(accountNumberPrefix.length());

        // Find where type code starts (SAV or CUR)
        int typeCodeIndex = -1;
        for (AccountType type : AccountType.values()) {
            int index = withoutPrefix.indexOf(type.getCode());
            if (index > 0) { // Must be after customer ID
                typeCodeIndex = index;
                break;
            }
        }

        if (typeCodeIndex == -1) {
            throw new RuntimeException("Could not parse account number format: " + originalNumber);
        }

        String customerIdPart = withoutPrefix.substring(0, typeCodeIndex);
        String typeCodePart = withoutPrefix.substring(typeCodeIndex, typeCodeIndex + 3); // SAV or CUR

        // Generate new sequence
        String newSequence = generateSequence();

        return accountNumberPrefix + customerIdPart + typeCodePart + newSequence;
    }

    /**
     * Validate account number format
     */
    public boolean isValidAccountNumberFormat(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 10) {
            return false;
        }

        // Must start with configured prefix
        if (!accountNumber.startsWith(accountNumberPrefix)) {
            return false;
        }

        // Must contain valid account type code
        boolean hasValidTypeCode = false;
        for (AccountType type : AccountType.values()) {
            if (accountNumber.contains(type.getCode())) {
                hasValidTypeCode = true;
                break;
            }
        }

        return hasValidTypeCode;
    }

    /**
     * Extract customer ID from account number
     */
    public Optional<Long> extractCustomerIdFromAccountNumber(String accountNumber) {
        try {
            if (!isValidAccountNumberFormat(accountNumber)) {
                return Optional.empty();
            }

            String withoutPrefix = accountNumber.substring(accountNumberPrefix.length());

            // Find where type code starts
            for (AccountType type : AccountType.values()) {
                int typeIndex = withoutPrefix.indexOf(type.getCode());
                if (typeIndex > 0) {
                    String customerIdStr = withoutPrefix.substring(0, typeIndex);
                    return Optional.of(Long.parseLong(customerIdStr));
                }
            }

            return Optional.empty();

        } catch (Exception e) {
            log.error("❌ Error extracting customer ID from account number: {}", accountNumber, e);
            return Optional.empty();
        }
    }

    /**
     * Extract account type from account number
     */
    public Optional<AccountType> extractAccountTypeFromAccountNumber(String accountNumber) {
        try {
            if (!isValidAccountNumberFormat(accountNumber)) {
                return Optional.empty();
            }

            for (AccountType type : AccountType.values()) {
                if (accountNumber.contains(type.getCode())) {
                    return Optional.of(type);
                }
            }

            return Optional.empty();

        } catch (Exception e) {
            log.error("❌ Error extracting account type from account number: {}", accountNumber, e);
            return Optional.empty();
        }
    }

    /**
     * Generate preview of what account number would look like (without saving)
     */
    public String previewAccountNumber(Long customerId, AccountType accountType) {
        return buildBaseAccountNumber(customerId, accountType);
    }

    /**
     * Batch validation of account numbers
     */
    public boolean areAllAccountNumbersValid(String... accountNumbers) {
        for (String accountNumber : accountNumbers) {
            if (!isValidAccountNumberFormat(accountNumber)) {
                return false;
            }
        }
        return true;
    }
}