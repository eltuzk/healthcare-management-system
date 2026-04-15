package com.healthcare.backend.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;

@Component
public class JwtServiceImpl implements JwtServiceInterface {
    private final static SecretKey SECRET_KEY = Jwts.SIG.HS256.key().build();
    private final static long EXPIRATION_TIME = 86400000;

    public String generateVerificationToken(String email) {
        return Jwts.builder()
                .claim("email", email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
    }

    public String generateToken(Long accountId, String email, String roleName) {
        return Jwts.builder()
                .subject(email)
                .claim("accountId", accountId)
                .claim("roleName", roleName)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 900000))
                .signWith(SECRET_KEY)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(SECRET_KEY)
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
                .verifyWith(SECRET_KEY)
                .build();
        
        return jwtParser.parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public String extractRole(String token) {
        JwtParser jwtParser = Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build();
        
        return jwtParser.parseSignedClaims(token)
                .getPayload()
                .get("roleName", String.class);
    }

    public Long extractAccountId(String token) {
        JwtParser jwtParser = Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build();
        
        return jwtParser.parseSignedClaims(token)
                .getPayload()
                .get("accountId", Long.class);
    }
}
