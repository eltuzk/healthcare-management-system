package com.healthcare.backend.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterResponse {
    private String email;
    
    public RegisterResponse() {
    }

    public RegisterResponse(String email) {
        this.email = email;
    }
}
