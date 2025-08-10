package com.bank.account.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("🔧 Configuring SecurityFilterChain for Account Service");

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-docs/**", "/v3/api-docs/**").permitAll()

                        // Admin endpoints - require ADMIN role
                        .requestMatchers("/api/accounts/admin/**").hasRole("ADMIN")

                        // Customer endpoints - require CUSTOMER or ADMIN role
                        .requestMatchers("/api/accounts/**").hasAnyRole("CUSTOMER", "ADMIN")

                        // Deny all other requests
                        .anyRequest().authenticated()
                );

        log.info("✅ SecurityFilterChain configured successfully for Account Service");
        return http.build();
    }
}