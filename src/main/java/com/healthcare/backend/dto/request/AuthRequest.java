package com.healthcare.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthRequest {
    @NotBlank(message = "Email là bắt buộc.")
    @Email(message = "Email không đúng định dạng. Ví dụ: abc@example.com")
    private String email;

    @NotBlank(message = "Mật khẩu là bắt buộc.")
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[0-9]).{8,}$",
        message = "Mật khẩu phải có ít nhất 8 ký tự, gồm ít nhất 1 chữ cái in hoa và 1 chữ số."
    )
    private String password;
}
