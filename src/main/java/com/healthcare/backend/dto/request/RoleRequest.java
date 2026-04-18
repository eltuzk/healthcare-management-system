package com.healthcare.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleRequest {
    @NotBlank(message = "Tên vai trò là bắt buộc")
    @Size(max = 100, message = "Tên vai trò không được vượt quá 100 ký tự")
    private String roleName;

    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
    private String description;
}
