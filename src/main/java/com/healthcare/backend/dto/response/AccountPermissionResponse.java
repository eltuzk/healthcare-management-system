package com.healthcare.backend.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountPermissionResponse {
    private Long accountId;
    private String email;
    private Long permissionId;
    private String permissionName;
}
