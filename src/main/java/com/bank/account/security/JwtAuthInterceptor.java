package com.bank.account.security;

import com.bank.account.util.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    /**
     * Get current authenticated user from ThreadLocal
     * This is now populated by JwtAuthenticationFilter
     */
    public static AuthenticatedUser getCurrentUser() {
        return JwtAuthenticationFilter.getCurrentUser();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // JWT processing is now handled by JwtAuthenticationFilter
        // This interceptor can be used for additional request processing if needed
        AuthenticatedUser currentUser = getCurrentUser();
        if (currentUser != null) {
            log.debug("🔍 Request from authenticated user: {} ({})",
                    currentUser.getUsername(), currentUser.getRole());
        }
        return true;
    }
}