package com.healthcare.backend.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleResponse {

    private Long roleId;
    private String roleName;
    private String description;
}
