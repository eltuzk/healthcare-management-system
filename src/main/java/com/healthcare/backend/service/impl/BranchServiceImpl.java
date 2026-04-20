package com.healthcare.backend.service.impl;

import com.healthcare.backend.dto.request.BranchRequest;
import com.healthcare.backend.dto.response.BranchResponse;
import com.healthcare.backend.entity.Branch;
import com.healthcare.backend.exception.DuplicateResourceException;
import com.healthcare.backend.exception.ResourceNotFoundException;
import com.healthcare.backend.mapper.BranchMapper;
import com.healthcare.backend.repository.BranchRepository;
import com.healthcare.backend.service.BranchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;
    private final BranchMapper branchMapper;

    @Override
    @Transactional(readOnly = true)
    public List<BranchResponse> getAll() {
        return branchRepository.findAll()
                .stream()
                .map(branchMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BranchResponse getById(Long id) {
        return branchMapper.toResponse(findOrThrow(id));
    }

    @Override
    @Transactional
    public BranchResponse create(BranchRequest request) {
        if (branchRepository.existsByBranchName(request.getBranchName())) {
            throw new DuplicateResourceException("Tên chi nhánh đã tồn tại: " + request.getBranchName());
        }

        Branch branch = branchMapper.toEntity(request);
        return branchMapper.toResponse(branchRepository.save(branch));
    }

    @Override
    @Transactional
    public BranchResponse update(Long id, BranchRequest request) {
        Branch branch = findOrThrow(id);

        if (request.getBranchName() != null
                && branchRepository.existsByBranchNameAndBranchIdNot(request.getBranchName(), id)) {
            throw new DuplicateResourceException("Tên chi nhánh đã tồn tại: " + request.getBranchName());
        }

        branchMapper.updateEntityFromRequest(request, branch);
        return branchMapper.toResponse(branchRepository.save(branch));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        findOrThrow(id);
        branchRepository.deleteById(id);
    }

    private Branch findOrThrow(Long id) {
        return branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi nhánh với id: " + id));
    }
}
