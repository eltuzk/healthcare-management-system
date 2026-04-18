package com.healthcare.backend.security;

public record UserPrincipal(Long accountId, String email, String role) {

}
