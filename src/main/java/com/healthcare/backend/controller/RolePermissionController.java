package com.healthcare.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.healthcare.backend.dto.request.RolePermissionRequest;
import com.healthcare.backend.dto.response.RolePermissionResponse;
import com.healthcare.backend.service.RolePermissionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/role-permissions")
@RequiredArgsConstructor
public class RolePermissionController {

    private final RolePermissionService rolePermissionService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<RolePermissionResponse> assign(@Valid @RequestBody RolePermissionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rolePermissionService.assign(request));
    }

    @DeleteMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> revoke(@Valid @RequestBody RolePermissionRequest request) {
        rolePermissionService.revoke(request);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/role/{roleId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<RolePermissionResponse>> getByRoleId(@PathVariable Long roleId) {
        return ResponseEntity.ok(rolePermissionService.getByRoleId(roleId));
    }
}
