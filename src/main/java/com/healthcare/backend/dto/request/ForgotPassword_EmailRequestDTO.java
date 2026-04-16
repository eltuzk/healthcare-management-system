package com.healthcare.backend.dto.request;

import jakarta.validation.constraints.Email;

public class ForgotPassword_EmailRequestDTO {
    @Email(message = "Invalid email format. (Example: abc@example.com)")
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    
}
