package com.healthcare.backend.service.impl;

import com.healthcare.backend.dto.request.LabTestRequest;
import com.healthcare.backend.dto.response.LabTestResponse;
import com.healthcare.backend.entity.LabTest;
import com.healthcare.backend.exception.DuplicateResourceException;
import com.healthcare.backend.exception.ResourceNotFoundException;
import com.healthcare.backend.mapper.LabTestMapper;
import com.healthcare.backend.repository.LabTestRepository;
import com.healthcare.backend.service.LabTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LabTestServiceImpl implements LabTestService {

    private final LabTestRepository labTestRepository;
    private final LabTestMapper labTestMapper;

    @Override
    @Transactional(readOnly = true)
    public List<LabTestResponse> getAllLabTests() {
        return labTestRepository.findAllByIsActiveOrderByLabTestNameAsc(1)
                .stream()
                .map(labTestMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LabTestResponse getLabTestById(Long id) {
        LabTest labTest = findActiveLabTestById(id);
        return labTestMapper.toResponse(labTest);
    }

    @Override
    @Transactional
    public LabTestResponse createLabTest(LabTestRequest request) {
        validateDuplicateLabTestName(request.getLabTestName(), null);

        LabTest labTest = labTestMapper.toEntity(request);
        LabTest savedLabTest = labTestRepository.save(labTest);

        return labTestMapper.toResponse(savedLabTest);
    }

    @Override
    @Transactional
    public LabTestResponse updateLabTest(Long id, LabTestRequest request) {
        LabTest labTest = findActiveLabTestById(id);

        validateDuplicateLabTestName(request.getLabTestName(), id);

        labTestMapper.updateEntityFromRequest(request, labTest);
        LabTest updatedLabTest = labTestRepository.save(labTest);

        return labTestMapper.toResponse(updatedLabTest);
    }

    @Override
    @Transactional
    public LabTestResponse deactivateLabTest(Long id) {
        LabTest labTest = findActiveLabTestById(id);

        labTest.setIsActive(0);
        LabTest deactivatedLabTest = labTestRepository.save(labTest);

        return labTestMapper.toResponse(deactivatedLabTest);
    }

    private LabTest findActiveLabTestById(Long id) {
        return labTestRepository.findByLabTestIdAndIsActive(id, 1)
                .orElseThrow(() -> new ResourceNotFoundException("Lab test not found with id: " + id));
    }

    private void validateDuplicateLabTestName(String labTestName, Long currentId) {
        String normalizedName = labTestName.trim();

        boolean duplicated = currentId == null
                ? labTestRepository.existsByLabTestNameIgnoreCase(normalizedName)
                : labTestRepository.existsByLabTestNameIgnoreCaseAndLabTestIdNot(normalizedName, currentId);

        if (duplicated) {
            throw new DuplicateResourceException("Lab test name already exists");
        }
    }
}