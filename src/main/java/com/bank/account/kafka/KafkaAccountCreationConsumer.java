package com.bank.account.kafka;

import com.bank.account.events.AccountCreationEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.bank.account.service.AdminAuthService;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaAccountCreationConsumer {

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final AdminAuthService adminAuthService;

    @KafkaListener(topics = "account-creation-topic", groupId = "account-service-group")
    public void consume(String message) {
        try {
            AccountCreationEvent event = objectMapper.readValue(message, AccountCreationEvent.class);
            log.info("Received account-creation event: {}", event);

            String adminJwt = adminAuthService.getAdminJwt();

            String url = "http://localhost:8083/api/accounts/admin/create-for-customer/" + event.getCustomerId();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminJwt);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (resp.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully triggered admin create for customer {}", event.getCustomerId());
            } else {
                log.error("Admin create returned non-2xx: {} / body: {}", resp.getStatusCode(), resp.getBody());
                // TODO: optionally push to DLQ or retry
            }
        } catch (Exception ex) {
            log.error("Error processing account creation event", ex);
            // TODO: push to DLQ or implement retry logic
        }
    }
}
