package com.healthcare.backend.service.impl;

import com.healthcare.backend.dto.request.SpecialtyRequest;
import com.healthcare.backend.dto.response.SpecialtyResponse;
import com.healthcare.backend.entity.Specialty;
import com.healthcare.backend.exception.DuplicateResourceException;
import com.healthcare.backend.exception.ResourceNotFoundException;
import com.healthcare.backend.mapper.SpecialtyMapper;
import com.healthcare.backend.repository.SpecialtyRepository;
import com.healthcare.backend.service.SpecialtyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SpecialtyServiceImpl implements SpecialtyService {

    private final SpecialtyRepository specialtyRepository;
    private final SpecialtyMapper specialtyMapper;

    @Override
    @Transactional
    public SpecialtyResponse create(SpecialtyRequest request) {
        validateDuplicates(request, null);
        return specialtyMapper.toResponse(specialtyRepository.save(specialtyMapper.toEntity(request)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SpecialtyResponse> getAll() {
        return specialtyRepository.findAllByOrderBySpecialtyNameAsc()
                .stream()
                .map(specialtyMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SpecialtyResponse getById(Long specialtyId) {
        return specialtyMapper.toResponse(findOrThrow(specialtyId));
    }

    @Override
    @Transactional
    public SpecialtyResponse update(Long specialtyId, SpecialtyRequest request) {
        Specialty specialty = findOrThrow(specialtyId);
        validateDuplicates(request, specialtyId);
        specialtyMapper.updateEntityFromRequest(request, specialty);
        return specialtyMapper.toResponse(specialtyRepository.save(specialty));
    }

    @Override
    @Transactional
    public SpecialtyResponse deactivate(Long specialtyId) {
        Specialty specialty = findOrThrow(specialtyId);
        specialty.setIsActive(0);
        return specialtyMapper.toResponse(specialtyRepository.save(specialty));
    }

    private Specialty findOrThrow(Long specialtyId) {
        return specialtyRepository.findById(specialtyId)
                .orElseThrow(() -> new ResourceNotFoundException("Specialty not found with id: " + specialtyId));
    }

    private void validateDuplicates(SpecialtyRequest request, Long specialtyId) {
        String specialtyCode = request.getSpecialtyCode().trim();
        String specialtyName = request.getSpecialtyName().trim();

        boolean duplicatedCode = specialtyId == null
                ? specialtyRepository.existsBySpecialtyCodeIgnoreCase(specialtyCode)
                : specialtyRepository.existsBySpecialtyCodeIgnoreCaseAndSpecialtyIdNot(specialtyCode, specialtyId);
        if (duplicatedCode) {
            throw new DuplicateResourceException("Specialty code already exists");
        }

        boolean duplicatedName = specialtyId == null
                ? specialtyRepository.existsBySpecialtyNameIgnoreCase(specialtyName)
                : specialtyRepository.existsBySpecialtyNameIgnoreCaseAndSpecialtyIdNot(specialtyName, specialtyId);
        if (duplicatedName) {
            throw new DuplicateResourceException("Specialty name already exists");
        }
    }
}
