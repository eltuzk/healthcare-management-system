package com.healthcare.backend.dto.response;
import lombok.*;
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class BranchResponseDto {
    private Integer branchId;
    private String branchName;
    private String branchAddress;
    private String branchHotline;
}
