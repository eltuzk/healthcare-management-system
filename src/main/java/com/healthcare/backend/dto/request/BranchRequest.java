package com.healthcare.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchRequest {

    @NotBlank(message = "Tên chi nhánh không được để trống")
    private String branchName;

    @NotBlank(message = "Địa chỉ chi nhánh không được để trống")
    private String branchAddress;

    private String branchHotline;
}
