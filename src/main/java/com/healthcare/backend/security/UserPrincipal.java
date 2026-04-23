package com.healthcare.backend.security;

import java.security.Principal;

public record UserPrincipal(Long accountId, String email, String role) implements Principal {

    @Override
    public String getName() {
        return email;
    }
}
