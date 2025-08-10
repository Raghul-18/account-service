package com.bank.account.security;

import com.bank.account.util.AuthenticatedUser;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.security.Key;

@Slf4j
@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private Key signingKey;

    @PostConstruct
    public void init() {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
        log.info("🔑 JWT Utils initialized successfully");
    }

    public boolean validateToken(String token) {
        try {
            getClaims(token);
            log.debug("✅ JWT token validation successful");
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("❌ JWT token expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("❌ Unsupported JWT token: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("❌ Malformed JWT token: {}", e.getMessage());
        } catch (SignatureException e) {
            log.warn("❌ Invalid JWT signature: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("❌ JWT token compact of handler are invalid: {}", e.getMessage());
        }
        return false;
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public AuthenticatedUser extractUser(String token) {
        Claims claims = getClaims(token);

        Long userId = Long.parseLong(claims.getSubject());
        String role = claims.get("role", String.class);
        String username = claims.get("username", String.class);

        log.debug("🔍 Extracted user from JWT - userId: {}, username: {}, role: {}",
                userId, username, role);

        return AuthenticatedUser.builder()
                .userId(userId)
                .role(role)
                .username(username)
                .build();
    }
}