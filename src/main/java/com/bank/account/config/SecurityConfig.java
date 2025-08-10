package com.bank.account.config;

import com.bank.account.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("🔧 Configuring SecurityFilterChain for Account Service with JWT support");

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Add JWT filter before Spring Security's authentication filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - no authentication required
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

        log.info("✅ SecurityFilterChain configured successfully with JWT authentication");
        return http.build();
    }
}