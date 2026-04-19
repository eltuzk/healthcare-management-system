package com.healthcare.backend.dto.response;

public class PermissionResponse {
    private Long id;
    private String permissionName;
    private String details;

    public PermissionResponse() {
    }
    
    public PermissionResponse(Long id, String permissionName, String details) {
        this.id = id;
        this.permissionName = permissionName;
        this.details = details;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPermissionName() {
        return permissionName;
    }

    public void setPermissionName(String permissionName) {
        this.permissionName = permissionName;
    }

    public String getDetails() {
        return details;
    }
    
    public void setDetails(String details) {
        this.details = details;
    }
}
