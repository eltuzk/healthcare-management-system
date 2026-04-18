package com.healthcare.backend.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtServiceImpl implements JwtServiceInterface {
    private static final long VERIFICATION_TOKEN_EXPIRATION_TIME = 86400000;
    private static final long ACCESS_TOKEN_EXPIRATION_TIME = 900000;
    private final SecretKey secretKey;

    public JwtServiceImpl(@Value("${security.jwt.secret:${JWT_SECRET:}}") String jwtSecret) {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalStateException("security.jwt.secret or JWT_SECRET must be configured.");
        }

        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("security.jwt.secret must be at least 32 characters for HS256.");
        }

        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateVerificationToken(String email) {
        return Jwts.builder()
                .claim("email", email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + VERIFICATION_TOKEN_EXPIRATION_TIME))
                .signWith(secretKey)
                .compact();
    }

    public String generateToken(Long accountId, String email, String roleName) {
        return Jwts.builder()
                .subject(email)
                .claim("accountId", accountId)
                .claim("roleName", roleName)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION_TIME))
                .signWith(secretKey)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return true;
        } catch (JwtException e) {
            System.err.println("Token is invalid.");
            return false;
        }
    }

    public String extractEmail(String token) {
        JwtParser jwtParser = Jwts.parser()
                .verifyWith(secretKey)
                .build();

        return jwtParser.parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public String extractRole(String token) {
        JwtParser jwtParser = Jwts.parser()
                .verifyWith(secretKey)
                .build();

        return jwtParser.parseSignedClaims(token)
                .getPayload()
                .get("roleName", String.class);
    }

    public Long extractAccountId(String token) {
        JwtParser jwtParser = Jwts.parser()
                .verifyWith(secretKey)
                .build();

        return jwtParser.parseSignedClaims(token)
                .getPayload()
                .get("accountId", Long.class);
    }
}
