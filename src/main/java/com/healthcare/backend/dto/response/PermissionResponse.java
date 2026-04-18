package com.healthcare.backend.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissionResponse {
    private Long permissionId;
    private String permissionName;
    private String detail;

    public PermissionResponse() {
    }

    public PermissionResponse(Long permissionId, String permissionName, String detail) {
        this.permissionId = permissionId;
        this.permissionName = permissionName;
        this.detail = detail;
    }
}
