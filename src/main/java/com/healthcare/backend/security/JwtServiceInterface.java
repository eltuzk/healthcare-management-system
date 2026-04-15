package com.healthcare.backend.security;

public interface JwtServiceInterface {
    String generateVerificationToken(String email);

    String generateToken(Long accountId, String email, String roleName);

    boolean validateToken(String token);

    String extractEmail(String token);

    String extractRole(String token);

    Long extractAccountId(String token);
}
