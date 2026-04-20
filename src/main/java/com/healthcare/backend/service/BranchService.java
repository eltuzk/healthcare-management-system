package com.healthcare.backend.service;

import com.healthcare.backend.dto.request.BranchRequest;
import com.healthcare.backend.dto.response.BranchResponse;

import java.util.List;

public interface BranchService {

    List<BranchResponse> getAll();

    BranchResponse getById(Long id);

    BranchResponse create(BranchRequest request);

    BranchResponse update(Long id, BranchRequest request);

    void delete(Long id);
}
