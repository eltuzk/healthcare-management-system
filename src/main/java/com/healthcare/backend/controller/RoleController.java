package com.healthcare.backend.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.healthcare.backend.dto.request.RoleRequest;
import com.healthcare.backend.dto.response.PermissionResponse;
import com.healthcare.backend.dto.response.RoleResponse;
import com.healthcare.backend.service.RoleService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/roles")
public class RoleController {
    @Autowired
    private RoleService roleService;

    @GetMapping
    public ResponseEntity<Page<RoleResponse>> getAllRoles(@ParameterObject Pageable pageable) {
        Page<RoleResponse> res = roleService.getAllRoles(pageable);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleResponse> getRoleById(@PathVariable Long id) {
        RoleResponse res = roleService.getRoleById(id);
        return ResponseEntity.ok(res);
    }

    @PostMapping
    public ResponseEntity<RoleResponse> createRole(@Valid @RequestBody RoleRequest roleRequest) {
        RoleResponse res = roleService.createRole(roleRequest);
        return ResponseEntity.ok(res);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoleResponse> updateRole(@PathVariable Long id, @Valid @RequestBody RoleRequest roleRequest) {
        RoleResponse res = roleService.updateRole(id, roleRequest);
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok("Role deleted successfully");
    }

    @PostMapping("/{roleId}/permissions/{permissionId}")
    public ResponseEntity<String> addPermissionToRole(@PathVariable Long roleId, @PathVariable Long permissionId) {
        roleService.addPermissisonToRole(roleId, permissionId);
        return ResponseEntity.ok("Permission added successfully.");
    }

    @GetMapping("/{roleId}/permissions")
    public ResponseEntity<Page<PermissionResponse>> getPermissionsByRole (@PathVariable Long roleId, @ParameterObject Pageable pageable) {
        Page<PermissionResponse> res = roleService.getPermissionsOfRole(roleId, pageable);
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    public ResponseEntity<String> removePermissionFromRole (@PathVariable Long roleId, @PathVariable Long permissionId) {
        roleService.removePermissionFromRole(roleId, permissionId);
        return ResponseEntity.ok("Permission deleted successfully.");
    }
}
