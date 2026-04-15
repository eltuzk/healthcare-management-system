package com.healthcare.backend.dto.response;

public class RegisterResponseDTO {
    private String email;
    
    public RegisterResponseDTO() {
    }

    public RegisterResponseDTO(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
