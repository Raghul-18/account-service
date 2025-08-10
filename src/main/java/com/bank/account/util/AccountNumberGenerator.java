package com.bank.account.util;

import org.springframework.stereotype.Component;

@Component
public class AccountNumberGenerator {

    private static final String BANK_CODE = "BANK1";

    /**
     * Generate account number in format: BANK1CUR001, BANK1SAV001
     * @param accountType CURRENT or SAVINGS
     * @param sequence Sequential number (1, 2, 3...)
     * @return Formatted account number
     */
    public String generateAccountNumber(AccountType accountType, Long sequence) {
        String typeCode = accountType.getCode(); // CUR or SAV
        String sequenceStr = String.format("%03d", sequence); // 001, 002, 003...
        return BANK_CODE + typeCode + sequenceStr;
    }

    /**
     * Extract sequence number from account number
     * @param accountNumber e.g., BANK1CUR001
     * @return sequence number (1)
     */
    public Long extractSequence(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 3) {
            return 0L;
        }
        String sequencePart = accountNumber.substring(accountNumber.length() - 3);
        try {
            return Long.parseLong(sequencePart);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    /**
     * Extract account type from account number
     * @param accountNumber e.g., BANK1CUR001
     * @return AccountType (CURRENT or SAVINGS)
     */
    public AccountType extractAccountType(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 8) {
            throw new IllegalArgumentException("Invalid account number format");
        }

        String typeCode = accountNumber.substring(5, 8); // Extract CUR or SAV
        return typeCode.equals("CUR") ? AccountType.CURRENT : AccountType.SAVINGS;
    }
}