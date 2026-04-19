package com.healthcare.backend.service.impl;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.healthcare.backend.dto.request.BranchRequest;
import com.healthcare.backend.dto.response.BranchResponse;
import com.healthcare.backend.repository.BranchRepository;
import com.healthcare.backend.service.BranchService;
import com.healthcare.backend.entity.*;
@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {
    private final BranchRepository branchRepository;
    private BranchResponse toResponse(Branch branch) {
        return BranchResponse.builder()
                .branchId(branch.getBranchId())
                .branchName(branch.getBranchName())
                .branchAddress(branch.getBranchAddress())
                .branchHotline(branch.getBranchHotline())
                .build();
    }
    private Branch toEntity(BranchRequest request) {
        return Branch.builder()
                .branchName(request.getBranchName())
                .branchAddress(request.getBranchAddress())
                .branchHotline(request.getBranchHotline())
                .build();
    }
        @Override
    public List<BranchResponse> getAll() {
        return branchRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    @Override
    public BranchResponse getbyId(Integer id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay chi nhanh: " + id));
        return toResponse(branch);
    }
    @Override
    public BranchResponse update(Integer id, BranchRequest request)
    {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay chi nhanh:  " + id));

        branch.setBranchName(request.getBranchName());
        branch.setBranchAddress(request.getBranchAddress());
        branch.setBranchHotline(request.getBranchHotline());

        return toResponse(branchRepository.save(branch));
    }
    @Override
    public BranchResponse create(BranchRequest BranchRequest)
    {
        Branch saved= branchRepository.save(toEntity(BranchRequest));
        return toResponse(saved);
    }
    @Override
    public void delete(Integer id) {
        if (!branchRepository.existsById(id)) {
            throw new RuntimeException("Khong tim thay chi nhanh " + id);
        }
        branchRepository.deleteById(id);
    }
}
