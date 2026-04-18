package com.healthcare.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequest {
    @NotBlank(message = "Mật khẩu cũ không được trống.")
    private String oldPassword;

    @NotBlank(message = "Mật khẩu mới không được trống.")
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[0-9]).{8,}$",
        message = "Mật khẩu phải có ít nhất 8 ký tự, gồm ít nhất 1 chữ cái in hoa và 1 chữ số."
    )
    private String newPassword;

    @NotBlank(message = "Trường này là bắt buộc.")
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[0-9]).{8,}$",
        message = "Mật khẩu phải có ít nhất 8 ký tự, gồm ít nhất 1 chữ cái in hoa và 1 chữ số."
    )
    private String confirmNewPassword;
}
