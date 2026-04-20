package com.healthcare.backend.service;

import java.util.List;
import com.healthcare.backend.dto.request.RoleRequest;
import com.healthcare.backend.dto.response.RoleResponse;

public interface RoleService {

    List<RoleResponse> getAll();

    RoleResponse getById(Long id);

    RoleResponse create(RoleRequest request);

    RoleResponse update(Long id, RoleRequest request);

    void delete(Long id);
}
