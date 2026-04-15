package com.healthcare.backend.dto.response;

public class AccountResponseDTO {
    private Long id;
    private String email;
    private String role;
    private boolean isActive;

    public AccountResponseDTO() {
    }

    public AccountResponseDTO(Long id, String email, String role, boolean isActive) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.isActive = isActive;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }
}
