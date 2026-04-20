package com.healthcare.backend.mapper;

import com.healthcare.backend.dto.request.BranchRequest;
import com.healthcare.backend.dto.response.BranchResponse;
import com.healthcare.backend.entity.Branch;
import org.springframework.stereotype.Component;

@Component
public class BranchMapper {

    public Branch toEntity(BranchRequest request) {
        Branch entity = new Branch();
        entity.setBranchName(request.getBranchName());
        entity.setBranchAddress(request.getBranchAddress());
        entity.setBranchHotline(request.getBranchHotline());
        return entity;
    }

    public BranchResponse toResponse(Branch entity) {
        BranchResponse response = new BranchResponse();
        response.setBranchId(entity.getBranchId());
        response.setBranchName(entity.getBranchName());
        response.setBranchAddress(entity.getBranchAddress());
        response.setBranchHotline(entity.getBranchHotline());
        return response;
    }

    public void updateEntityFromRequest(BranchRequest request, Branch entity) {
        if (request.getBranchName() != null) entity.setBranchName(request.getBranchName());
        if (request.getBranchAddress() != null) entity.setBranchAddress(request.getBranchAddress());
        if (request.getBranchHotline() != null) entity.setBranchHotline(request.getBranchHotline());
    }
}
