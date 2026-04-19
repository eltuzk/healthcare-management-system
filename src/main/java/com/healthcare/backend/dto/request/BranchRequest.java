package com.healthcare.backend.dto.request;
import lombok.*;
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class BranchRequest {
    private Integer branchId;
    private String branchName;
    private String branchAddress;
    private String branchHotline;
}

