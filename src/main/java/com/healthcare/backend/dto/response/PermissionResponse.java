package com.healthcare.backend.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissionResponse {
    private Long id;
    private String permissionName;
    private String detail;

    public PermissionResponse() {
    }

    public PermissionResponse(Long id, String permissionName, String detail) {
        this.id = id;
        this.permissionName = permissionName;
        this.detail = detail;
    }
}
