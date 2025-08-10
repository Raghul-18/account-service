// =============================================================================
// 1. JwtAuthenticationFilter.java - NEW FILE
// =============================================================================
package com.bank.account.security;

import com.bank.account.util.AuthenticatedUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private static final ThreadLocal<AuthenticatedUser> userHolder = new ThreadLocal<>();

    public static AuthenticatedUser getCurrentUser() {
        return userHolder.get();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        log.debug("🔍 Processing request: {} {}", request.getMethod(), requestURI);

        // Skip JWT processing for public endpoints
        if (isPublicEndpoint(requestURI)) {
            log.debug("⏭️ Skipping JWT processing for public endpoint: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            log.debug("🔑 Found JWT token in request");

            try {
                if (jwtUtils.validateToken(token)) {
                    AuthenticatedUser user = jwtUtils.extractUser(token);
                    log.debug("✅ Valid JWT token for user: {} with role: {}", user.getUsername(), user.getRole());

                    // Store user in ThreadLocal for controller access
                    userHolder.set(user);

                    // Create Spring Security Authentication with proper role format
                    List<GrantedAuthority> authorities = List.of(
                            new SimpleGrantedAuthority("ROLE_" + user.getRole())
                    );

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(user, null, authorities);

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("🔐 Set security context for user: {} with authorities: {}",
                            user.getUsername(), authorities);

                } else {
                    log.warn("❌ Invalid JWT token");
                }
            } catch (Exception e) {
                log.error("❌ JWT processing error: {}", e.getMessage());
            }
        } else {
            log.debug("⚠️ No Authorization header found or invalid format");
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Clean up ThreadLocal to prevent memory leaks
            userHolder.remove();
            SecurityContextHolder.clearContext();
        }
    }

    private boolean isPublicEndpoint(String requestURI) {
        return requestURI.startsWith("/error") ||
                requestURI.startsWith("/actuator") ||
                requestURI.startsWith("/swagger-ui") ||
                requestURI.equals("/swagger-ui.html") ||
                requestURI.startsWith("/v3/api-docs") ||
                requestURI.startsWith("/swagger-resources") ||
                requestURI.startsWith("/webjars");
    }

}
