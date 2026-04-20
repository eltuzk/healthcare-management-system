package com.healthcare.backend.service;

import java.util.List;

import com.healthcare.backend.dto.request.RolePermissionRequest;
import com.healthcare.backend.dto.response.RolePermissionResponse;

public interface RolePermissionService {

    RolePermissionResponse assign(RolePermissionRequest request);

    void revoke(RolePermissionRequest request);

    List<RolePermissionResponse> getByRoleId(Long roleId);
}
