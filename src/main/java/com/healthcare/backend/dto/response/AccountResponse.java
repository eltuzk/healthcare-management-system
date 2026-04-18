package com.healthcare.backend.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountResponse {
    private Long accountId;
    private String email;
    private String roleName;
    private boolean isActive;

    public AccountResponse() {
    }

    public AccountResponse(Long accountId, String email, String roleName, boolean isActive) {
        this.accountId = accountId;
        this.email = email;
        this.roleName = roleName;
        this.isActive = isActive;
    }
}
