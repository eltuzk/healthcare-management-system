package com.healthcare.backend.dto.response;

public class RegisterResponse {
    private String email;
    
    public RegisterResponse() {
    }

    public RegisterResponse(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
