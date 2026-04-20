package com.healthcare.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountPermissionRequest {

    @NotNull(message = "Account ID is required")
    private Long accountId;

    @NotNull(message = "Permission ID is required")
    private Long permissionId;
}
