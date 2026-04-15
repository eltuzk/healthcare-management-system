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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.healthcare.backend.dto.request.PermissionRequestDTO;
import com.healthcare.backend.dto.response.PermissionResponseDTO;
import com.healthcare.backend.service.PermissionServiceInterface;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/permissions")
public class PermissionController {
    @Autowired
    private PermissionServiceInterface permissionService;

    @GetMapping
    public ResponseEntity<Page<PermissionResponseDTO>> getAllPermissions(@ParameterObject Pageable pageable) {
        Page<PermissionResponseDTO> res = permissionService.getAllPermissions(pageable);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PermissionResponseDTO> getPermissionById(@PathVariable Long id) {
        PermissionResponseDTO res = permissionService.getPermissionById(id);
        return ResponseEntity.ok(res);
    }

    @PostMapping
    public ResponseEntity<PermissionResponseDTO> createPermission(@Valid @RequestBody PermissionRequestDTO permissionRequestDTO) {
        PermissionResponseDTO res = permissionService.createPermission(permissionRequestDTO);
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePermission(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return ResponseEntity.ok("Permission deleted successfully");
    }
}
