package com.healthcare.backend.service.impl;

import com.healthcare.backend.dto.request.MedicalServiceRequest;
import com.healthcare.backend.dto.response.MedicalServiceResponse;
import com.healthcare.backend.entity.MedicalService;
import com.healthcare.backend.exception.DuplicateResourceException;
import com.healthcare.backend.exception.ResourceNotFoundException;
import com.healthcare.backend.mapper.MedicalServiceMapper;
import com.healthcare.backend.repository.MedicalServiceRepository;
import com.healthcare.backend.service.MedicalServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicalServiceServiceImpl implements MedicalServiceService {

    private final MedicalServiceRepository medicalServiceRepository;
    private final MedicalServiceMapper medicalServiceMapper;

    @Override
    @Transactional(readOnly = true)
    public List<MedicalServiceResponse> getAllMedicalServices() {
        return medicalServiceRepository.findAllByIsActiveOrderByMedicalServiceNameAsc(1)
                .stream()
                .map(medicalServiceMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MedicalServiceResponse getMedicalServiceById(Long id) {
        MedicalService medicalService = findActiveMedicalServiceById(id);
        return medicalServiceMapper.toResponse(medicalService);
    }

    @Override
    @Transactional
    public MedicalServiceResponse createMedicalService(MedicalServiceRequest request) {
        validateDuplicateMedicalServiceName(request.getMedicalServiceName(), null);

        MedicalService medicalService = medicalServiceMapper.toEntity(request);
        MedicalService savedMedicalService = medicalServiceRepository.save(medicalService);

        return medicalServiceMapper.toResponse(savedMedicalService);
    }

    @Override
    @Transactional
    public MedicalServiceResponse updateMedicalService(Long id, MedicalServiceRequest request) {
        MedicalService medicalService = findActiveMedicalServiceById(id);

        validateDuplicateMedicalServiceName(request.getMedicalServiceName(), id);

        medicalServiceMapper.updateEntityFromRequest(request, medicalService);
        MedicalService updatedMedicalService = medicalServiceRepository.save(medicalService);

        return medicalServiceMapper.toResponse(updatedMedicalService);
    }

    @Override
    @Transactional
    public MedicalServiceResponse deactivateMedicalService(Long id) {
        MedicalService medicalService = findActiveMedicalServiceById(id);

        medicalService.setIsActive(0);
        MedicalService deactivatedMedicalService = medicalServiceRepository.save(medicalService);

        return medicalServiceMapper.toResponse(deactivatedMedicalService);
    }

    private MedicalService findActiveMedicalServiceById(Long id) {
        return medicalServiceRepository.findByMedServiceIdAndIsActive(id, 1)
                .orElseThrow(() -> new ResourceNotFoundException("Medical service not found with id: " + id));
    }

    private void validateDuplicateMedicalServiceName(String medicalServiceName, Long currentId) {
        String normalizedName = medicalServiceName.trim();

        boolean duplicated = currentId == null
                ? medicalServiceRepository.existsByMedicalServiceNameIgnoreCase(normalizedName)
                : medicalServiceRepository.existsByMedicalServiceNameIgnoreCaseAndMedServiceIdNot(
                        normalizedName,
                        currentId
                );

        if (duplicated) {
            throw new DuplicateResourceException("Medical service name already exists");
        }
    }
}