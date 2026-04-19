package com.healthcare.backend.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RolePermissionResponse {

    private Long roleId;
    private String roleName;
    private Long permissionId;
    private String permissionName;
}
