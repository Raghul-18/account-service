// src/main/java/com/bank/account/security/JwtAuthInterceptor.java
package com.bank.account.security;

import com.bank.account.util.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;
    private static final ThreadLocal<AuthenticatedUser> userHolder = new ThreadLocal<>();

    public static AuthenticatedUser getCurrentUser() {
        return userHolder.get();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtUtils.validateToken(token)) {
                AuthenticatedUser user = jwtUtils.extractUser(token);
                userHolder.set(user);
                log.debug("🔑 Authenticated user: {} with role: {}", user.getUsername(), user.getRole());
                return true;
            }
        }

        log.warn("❌ Unauthorized request to: {}", request.getRequestURI());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("Unauthorized or invalid token");
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        userHolder.remove();
    }
}