package com.healthcare.backend.dto.request;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.Email;
@Getter
@Setter
public class ForgotPasswordRequest {
    @Email(message = "Email không hợp lệ")
    private String email;
}



