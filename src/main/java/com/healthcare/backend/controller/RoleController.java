package com.healthcare.backend.controller;

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

import com.healthcare.backend.dto.request.RoleRequestDTO;
import com.healthcare.backend.dto.response.RoleResponseDTO;
import com.healthcare.backend.service.RolePermissionServiceInterface;
import com.healthcare.backend.service.RoleServiceInterface;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/roles")
public class RoleController {
    @Autowired
    private RoleServiceInterface roleService;

    @Autowired
    private RolePermissionServiceInterface rolePermissionService;
    @GetMapping
    public ResponseEntity<Page<RoleResponseDTO>> getAllRole(Pageable pageable) {
        Page<RoleResponseDTO> res = roleService.getAllRole(pageable);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleResponseDTO> getRoleById(@PathVariable Long id) {
        RoleResponseDTO res = roleService.getRoleById(id);
        return ResponseEntity.ok(res);
    }

    @PostMapping
    public ResponseEntity<RoleResponseDTO> createRole(@Valid @RequestBody RoleRequestDTO roleRequestDTO) {
        RoleResponseDTO res = roleService.createRole(roleRequestDTO);
        return ResponseEntity.ok(res);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoleResponseDTO> updateRole(@PathVariable Long id, @Valid @RequestBody RoleRequestDTO roleRequestDTO) {
        RoleResponseDTO res = roleService.updateRole(id, roleRequestDTO);
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok("Role deleted successfully");
    }

    @PostMapping("/{roleId}/permissions/{permissionId}")
    public ResponseEntity<String> addPermissionToRole(@PathVariable Long roleId, @PathVariable Long permissionId) {
        rolePermissionService.addPermissisonToRole(roleId, permissionId);
        return ResponseEntity.ok("Permission added successfully.");
    }
}
