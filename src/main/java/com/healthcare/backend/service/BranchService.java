package com.healthcare.backend.service;
import com.healthcare.backend.dto.request.BranchRequest;
import com.healthcare.backend.dto.response.BranchResponse;

import java.util.List;
public interface BranchService {
    List<BranchResponse> getAll();
    BranchResponse getbyId(Integer id);
    BranchResponse create(BranchRequest requestDto);
    BranchResponse update(Integer id, BranchRequest requestDto);
    void delete (Integer id);

} 
