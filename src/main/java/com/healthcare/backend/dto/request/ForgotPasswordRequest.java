package com.healthcare.backend.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForgotPasswordRequest {
    @Email(message = "Email không đúng định dạng. Ví dụ: abc@example.com")
    private String email;
}
