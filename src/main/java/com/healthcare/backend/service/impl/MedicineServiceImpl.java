package com.healthcare.backend.service.impl;

import com.healthcare.backend.dto.request.MedicineRequest;
import com.healthcare.backend.dto.response.MedicineResponse;
import com.healthcare.backend.entity.Medicine;
import com.healthcare.backend.exception.DuplicateResourceException;
import com.healthcare.backend.exception.ResourceNotFoundException;
import com.healthcare.backend.mapper.MedicineMapper;
import com.healthcare.backend.repository.MedicineRepository;
import com.healthcare.backend.service.MedicineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicineServiceImpl implements MedicineService {

    private final MedicineRepository medicineRepository;
    private final MedicineMapper medicineMapper;

    @Override
    @Transactional(readOnly = true)
    public List<MedicineResponse> getAllMedicines() {
        return medicineRepository.findAllByIsActiveOrderByMedicineNameAsc(1)
                .stream()
                .map(medicineMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MedicineResponse getMedicineById(Long id) {
        Medicine medicine = findActiveMedicineById(id);
        return medicineMapper.toResponse(medicine);
    }

    @Override
    @Transactional
    public MedicineResponse createMedicine(MedicineRequest request) {
        validateDuplicateMedicineName(request.getMedicineName(), null);

        Medicine medicine = medicineMapper.toEntity(request);
        Medicine savedMedicine = medicineRepository.save(medicine);

        return medicineMapper.toResponse(savedMedicine);
    }

    @Override
    @Transactional
    public MedicineResponse updateMedicine(Long id, MedicineRequest request) {
        Medicine medicine = findActiveMedicineById(id);

        validateDuplicateMedicineName(request.getMedicineName(), id);

        medicineMapper.updateEntityFromRequest(request, medicine);
        Medicine updatedMedicine = medicineRepository.save(medicine);

        return medicineMapper.toResponse(updatedMedicine);
    }

    @Override
    @Transactional
    public MedicineResponse deactivateMedicine(Long id) {
        Medicine medicine = findActiveMedicineById(id);

        medicine.setIsActive(0);
        Medicine deactivatedMedicine = medicineRepository.save(medicine);

        return medicineMapper.toResponse(deactivatedMedicine);
    }

    private Medicine findActiveMedicineById(Long id) {
        return medicineRepository.findByMedicineIdAndIsActive(id, 1)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with id: " + id));
    }

    private void validateDuplicateMedicineName(String medicineName, Long currentId) {
        String normalizedName = medicineName.trim();

        boolean duplicated = currentId == null
                ? medicineRepository.existsByMedicineNameIgnoreCase(normalizedName)
                : medicineRepository.existsByMedicineNameIgnoreCaseAndMedicineIdNot(normalizedName, currentId);

        if (duplicated) {
            throw new DuplicateResourceException("Medicine name already exists");
        }
    }
}