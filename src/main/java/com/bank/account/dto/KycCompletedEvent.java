package com.bank.account.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KycCompletedEvent {

    private Long customerId;
    private String eventType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    // Additional event metadata
    private String source; // "kyc-service"
    private String version; // "1.0"

    public static KycCompletedEvent create(Long customerId) {
        return KycCompletedEvent.builder()
                .customerId(customerId)
                .eventType("KYC_COMPLETED")
                .timestamp(LocalDateTime.now())
                .source("kyc-service")
                .version("1.0")
                .build();
    }
}