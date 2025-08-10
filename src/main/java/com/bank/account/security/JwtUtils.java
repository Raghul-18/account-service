package com.bank.account.security;

import com.bank.account.util.AuthenticatedUser;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;

/**
 * JWT utility class for token validation and user extraction
 * Used by JwtAuthInterceptor to authenticate requests
 */
@Slf4j
@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private Key signingKey;

    @PostConstruct
    public void init() {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
        log.info("🔐 JWT Utils initialized successfully");
    }

    /**
     * Validate JWT token
     * @param token JWT token
     * @return true if token is valid
     */
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("❌ Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extract claims from JWT token
     * @param token JWT token
     * @return Claims object
     */
    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Extract authenticated user from JWT token
     * @param token JWT token
     * @return AuthenticatedUser object
     */
    public AuthenticatedUser extractUser(String token) {
        try {
            Claims claims = getClaims(token);

            return AuthenticatedUser.builder()
                    .userId(Long.parseLong(claims.getSubject()))
                    .role(claims.get("role", String.class))
                    .username(claims.get("username", String.class))
                    .build();

        } catch (Exception e) {
            log.error("❌ Error extracting user from token: {}", e.getMessage());
            throw new JwtException("Failed to extract user from token", e);
        }
    }

    /**
     * Extract user ID from token
     * @param token JWT token
     * @return User ID
     */
    public Long extractUserId(String token) {
        return Long.parseLong(getClaims(token).getSubject());
    }

    /**
     * Extract role from token
     * @param token JWT token
     * @return User role
     */
    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    /**
     * Extract username from token
     * @param token JWT token
     * @return Username
     */
    public String extractUsername(String token) {
        return getClaims(token).get("username", String.class);
    }

    /**
     * Check if token is expired
     * @param token JWT token
     * @return true if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaims(token);
            return claims.getExpiration().before(new java.util.Date());
        } catch (Exception e) {
            log.debug("❌ Error checking token expiration: {}", e.getMessage());
            return true; // Consider expired if we can't parse
        }
    }

    /**
     * Get token expiration time in milliseconds
     * @param token JWT token
     * @return Expiration time
     */
    public long getExpirationTime(String token) {
        try {
            Claims claims = getClaims(token);
            return claims.getExpiration().getTime();
        } catch (Exception e) {
            log.error("❌ Error getting expiration time: {}", e.getMessage());
            return 0;
        }
    }
}