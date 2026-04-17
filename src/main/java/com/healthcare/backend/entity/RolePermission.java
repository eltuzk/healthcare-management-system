package com.healthcare.backend.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "role_permission")
public class RolePermission {
    @EmbeddedId
    private RolePermissionId rolePermissionId;

    @ManyToOne
    @MapsId("roleId")
    @JoinColumn(name = "role_id")
    private Role role;

    @ManyToOne
    @MapsId("permissionId")
    @JoinColumn(name = "permission_id")
    private Permission permission;

    public RolePermission() {
    }

    public RolePermission(RolePermissionId rolePermissionId, Role role, Permission permission) {
        this.rolePermissionId = rolePermissionId;
        this.role = role;
        this.permission = permission;
    }

    public RolePermissionId getRolePermissionID() {
        return rolePermissionId;
    }

    public void setRolePermissionID(RolePermissionId rolePermissionID) {
        this.rolePermissionId = rolePermissionID;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }
}
