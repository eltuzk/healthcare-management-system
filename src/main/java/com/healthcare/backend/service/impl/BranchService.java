package com.healthcare.backend.service.impl;
import lombok.RequiredArgsConstructor;
import lombok.*;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.healthcare.backend.dto.request.BranchRequestDto;
import com.healthcare.backend.dto.response.BranchResponseDto;
import com.healthcare.backend.repository.BranchRepository;
import com.healthcare.backend.service.IBranchService;
import com.healthcare.backend.entity.*;
@Service
@RequiredArgsConstructor
public class BranchService implements IBranchService {
    private final BranchRepository branchRepository;
    private BranchResponseDto toResponse(Branch branch) {
        return BranchResponseDto.builder()
                .branchId(branch.getBranchId())
                .branchName(branch.getBranchName())
                .branchAddress(branch.getBranchAddress())
                .branchHotline(branch.getBranchHotline())
                .build();
    }
    private Branch toEntity(BranchRequestDto request) {
        return Branch.builder()
                .branchName(request.getBranchName())
                .branchAddress(request.getBranchAddress())
                .branchHotline(request.getBranchHotline())
                .build();
    }
        @Override
    public List<BranchResponseDto> getAll() {
        return branchRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    @Override
    public BranchResponseDto getbyId(Integer id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay chi nhanh: " + id));
        return toResponse(branch);
    }
    @Override
    public BranchResponseDto update(Integer id, BranchRequestDto request)
    {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay chi nhanh:  " + id));

        branch.setBranchName(request.getBranchName());
        branch.setBranchAddress(request.getBranchAddress());
        branch.setBranchHotline(request.getBranchHotline());

        return toResponse(branchRepository.save(branch));
    }
    @Override
    public BranchResponseDto create(BranchRequestDto BranchRequestDto)
    {
        Branch saved= branchRepository.save(toEntity(BranchRequestDto));
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
