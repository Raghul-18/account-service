package com.bank.account.security;

import com.bank.account.util.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT Authentication Interceptor
 * Validates JWT tokens and sets user context for requests
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;

    // ThreadLocal to store current user for the request
    private static final ThreadLocal<AuthenticatedUser> userHolder = new ThreadLocal<>();

    /**
     * Get current authenticated user from ThreadLocal
     * @return Current authenticated user or null if not authenticated
     */
    public static AuthenticatedUser getCurrentUser() {
        return userHolder.get();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        String method = request.getMethod();
        String uri = request.getRequestURI();

        log.debug("🔍 Processing {} request to {}", method, uri);

        // Skip OPTIONS requests (CORS preflight)
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        // Extract Authorization header
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("❌ Missing or invalid Authorization header for {}", uri);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":{\"code\":\"UNAUTHORIZED\",\"message\":\"Missing or invalid Authorization header\"}}");
            return false;
        }

        // Extract token
        String token = authHeader.substring(7); // Remove "Bearer " prefix

        try {
            // Validate token
            if (!jwtUtils.validateToken(token)) {
                log.warn("❌ Invalid JWT token for request to {}", uri);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":{\"code\":\"INVALID_TOKEN\",\"message\":\"Invalid JWT token\"}}");
                return false;
            }

            // Extract user from token
            AuthenticatedUser user = jwtUtils.extractUser(token);

            if (user == null) {
                log.warn("❌ Could not extract user from token for request to {}", uri);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":{\"code\":\"INVALID_TOKEN\",\"message\":\"Could not extract user from token\"}}");
                return false;
            }

            // Set user in ThreadLocal
            userHolder.set(user);

            log.debug("✅ Authenticated user: {} (role: {}) for {}", user.getUsername(), user.getRole(), uri);
            return true;

        } catch (Exception e) {
            log.error("❌ JWT authentication error for {}: {}", uri, e.getMessage());

            // Clear any existing user context
            userHolder.remove();

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":{\"code\":\"AUTH_ERROR\",\"message\":\"Authentication failed: " + e.getMessage() + "\"}}");
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) throws Exception {
        // Clean up ThreadLocal to prevent memory leaks
        userHolder.remove();

        if (ex != null) {
            log.error("❌ Request completed with exception: {}", ex.getMessage());
        } else {
            log.debug("✅ Request completed successfully: {} {}", request.getMethod(), request.getRequestURI());
        }
    }

    /**
     * Validate if current user is admin
     * @throws SecurityException if user is not admin
     */
    public static void requireAdmin() {
        AuthenticatedUser user = getCurrentUser();
        if (user == null || !user.isAdmin()) {
            throw new SecurityException("Admin access required");
        }
    }

    /**
     * Validate if current user can access customer data
     * @param customerId Customer ID to check access for
     * @throws SecurityException if access is denied
     */
    public static void requireCustomerAccess(Long customerId) {
        AuthenticatedUser user = getCurrentUser();
        if (user == null) {
            throw new SecurityException("Authentication required");
        }

        if (!user.canAccessCustomer(customerId)) {
            throw new SecurityException("Access denied for customer: " + customerId);
        }
    }

    /**
     * Get current user or throw exception if not authenticated
     * @return Current authenticated user
     * @throws SecurityException if not authenticated
     */
    public static AuthenticatedUser requireAuthentication() {
        AuthenticatedUser user = getCurrentUser();
        if (user == null) {
            throw new SecurityException("Authentication required");
        }
        return user;
    }
}