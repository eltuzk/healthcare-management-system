package com.healthcare.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissionRequest {
    @NotBlank(message = "Tên quyền là bắt buộc")
    @Size(max = 100, message = "Tên quyền không được vượt quá 100 ký tự")
    private String permissionName;

    @Size(max = 500, message = "Mô tả quyền không được vượt quá 500 ký tự")
    private String detail;
}
