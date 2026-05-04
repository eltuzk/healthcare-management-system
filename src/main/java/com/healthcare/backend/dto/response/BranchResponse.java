package com.healthcare.backend.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchResponse {

    private Long branchId;
    private String branchName;
    private String branchAddress;
    private String branchHotline;
}
