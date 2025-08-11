package com.bank.account.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminAuthServiceImpl implements com.bank.account.service.AdminAuthService {

    private final RestTemplate restTemplate;

    @Value("${gateway.url:http://localhost:8080}")
    private String gatewayUrl;

    @Value("${gateway.admin.username:admin}")
    private String adminUsername;

    @Value("${gateway.admin.password:admin123}")
    private String adminPassword;

    // cached token holder
    private final AtomicReference<String> cachedToken = new AtomicReference<>(null);
    private volatile Instant tokenExpiry = Instant.EPOCH;

    @Override
    public synchronized String getAdminJwt() {
        // if token valid for next 30 seconds, return cached
        if (cachedToken.get() != null && Instant.now().isBefore(tokenExpiry.minusSeconds(30))) {
            return cachedToken.get();
        }

        String url = gatewayUrl + "/api/auth/admin-login"; // adapt if your gateway exposes different path
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> body = Map.of(
                    "username", adminUsername,
                    "password", adminPassword
            );

            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> resp = restTemplate.postForEntity(url, request, Map.class);

            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                Object tokenObj = resp.getBody().get("token");
                Object expObj = resp.getBody().get("exp"); // if gateway returns exp (epoch sec) — optional
                String token = tokenObj != null ? tokenObj.toString() : null;

                // Determine expiry: if gateway returns "exp" use it, else set 10 minutes default
                Instant expiry = Instant.now().plusSeconds(600);
                if (expObj != null) {
                    try {
                        long expSec = Long.parseLong(expObj.toString());
                        expiry = Instant.ofEpochSecond(expSec);
                    } catch (Exception ignored) {}
                }

                if (token != null) {
                    cachedToken.set(token);
                    tokenExpiry = expiry;
                    log.info("Fetched admin jwt, expires at {}", tokenExpiry);
                    return token;
                }
            }
            log.error("Failed to fetch admin token, status: {}", resp.getStatusCode());
        } catch (Exception ex) {
            log.error("Exception while fetching admin jwt", ex);
        }

        throw new IllegalStateException("Could not obtain admin JWT from gateway");
    }
}
