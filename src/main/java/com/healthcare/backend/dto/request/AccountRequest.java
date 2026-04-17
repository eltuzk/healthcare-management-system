package com.healthcare.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class AccountRequest {
    @NotBlank(message = "Email is required.")
    @Email(message = "Invalid email format. (Example: abc@example.com)")
    private String email;

    @NotBlank(message = "Password is required.")
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[0-9]).{8,}$",
        message = "Password must be at least 8 characters long, containing at least one uppercase letter, one number, and one special character."
    )
    private String password;
    @NotBlank(message = "Role is required.")
    private String role;

    @NotBlank(message = "Active status is required.")
    private boolean active;
    
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }    
}
