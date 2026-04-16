package com.healthcare.backend.dto.request;
import lombok.*;
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class BranchRequestDto {
    private Integer branchId;
    private String branchName;
    private String branchAddress;
    private String branchHotline;
}

