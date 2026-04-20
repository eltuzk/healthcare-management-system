package com.healthcare.backend.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountResponse {
    private Long accountId;
    private String email;
    private Long roleId;
    private String roleName;
    private Integer isActive;
}
