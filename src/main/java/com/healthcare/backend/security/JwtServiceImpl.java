package com.healthcare.backend.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
    private final SecretKey secretKey;
    private final long accessTokenExpirationMs;
    private final long verificationTokenExpirationMs;

    public JwtServiceImpl(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration-ms}") long accessTokenExpirationMs,
            @Value("${jwt.verification-token-expiration-ms}") long verificationTokenExpirationMs
    ) {
        this.secretKey = Keys.hmacShaKeyFor(hashSecret(secret));
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.verificationTokenExpirationMs = verificationTokenExpirationMs;
    }

    public String generateVerificationToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + verificationTokenExpirationMs))
                .signWith(secretKey)
                .compact();
    }

    public String generateToken(Long accountId, String email, String roleName) {
        return Jwts.builder()
                .subject(email)
                .claim("accountId", accountId)
                .claim("roleName", roleName)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpirationMs))
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
        }
        catch(JwtException e) {
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

        Object accountId = jwtParser.parseSignedClaims(token)
                .getPayload()
                .get("accountId");

        if (accountId instanceof Number number) {
            return number.longValue();
        }

        if (accountId instanceof String accountIdText && !accountIdText.isBlank()) {
            return Long.valueOf(accountIdText);
        }

        throw new JwtException("Token does not contain a valid accountId claim");
    }

    private byte[] hashSecret(String secret) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(secret.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm is not available", ex);
        }
    }
}
