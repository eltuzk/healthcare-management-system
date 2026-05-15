package com.healthcare.backend.service.impl;

import com.healthcare.backend.dto.request.MedicineLotRequest;
import com.healthcare.backend.dto.response.MedicineLotResponse;
import com.healthcare.backend.entity.Medicine;
import com.healthcare.backend.entity.MedicineLot;
import com.healthcare.backend.exception.BusinessException;
import com.healthcare.backend.exception.DuplicateResourceException;
import com.healthcare.backend.exception.ResourceNotFoundException;
import com.healthcare.backend.mapper.MedicineLotMapper;
import com.healthcare.backend.repository.MedicineLotRepository;
import com.healthcare.backend.repository.MedicineRepository;
import com.healthcare.backend.service.MedicineLotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicineLotServiceImpl implements MedicineLotService {

    private final MedicineLotRepository medicineLotRepository;
    private final MedicineRepository medicineRepository;
    private final MedicineLotMapper medicineLotMapper;

    @Override
    @Transactional(readOnly = true)
    public List<MedicineLotResponse> getAllMedicineLots(Long medicineId) {
        List<MedicineLot> medicineLots = medicineId == null
                ? medicineLotRepository.findAllByIsActiveOrderByExpiryDateAsc(1)
                : medicineLotRepository.findAllByMedicine_MedicineIdAndIsActiveOrderByExpiryDateAsc(medicineId, 1);

        return medicineLots.stream()
                .map(medicineLotMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MedicineLotResponse getMedicineLotById(Long id) {
        MedicineLot medicineLot = findActiveMedicineLotById(id);
        return medicineLotMapper.toResponse(medicineLot);
    }

    @Override
    @Transactional
    public MedicineLotResponse createMedicineLot(MedicineLotRequest request) {
        Medicine medicine = findActiveMedicineById(request.getMedicineId());

        validateMedicineLotRequest(request);
        validateDuplicateLotNumber(request.getMedicineId(), request.getLotNumber(), null);

        MedicineLot medicineLot = medicineLotMapper.toEntity(request, medicine);
        MedicineLot savedMedicineLot = medicineLotRepository.save(medicineLot);

        return medicineLotMapper.toResponse(savedMedicineLot);
    }

    @Override
    @Transactional
    public MedicineLotResponse updateMedicineLot(Long id, MedicineLotRequest request) {
        MedicineLot medicineLot = findActiveMedicineLotById(id);
        Medicine medicine = findActiveMedicineById(request.getMedicineId());

        validateMedicineLotRequest(request);
        validateDuplicateLotNumber(request.getMedicineId(), request.getLotNumber(), id);

        medicineLotMapper.updateEntityFromRequest(request, medicineLot, medicine);
        MedicineLot updatedMedicineLot = medicineLotRepository.save(medicineLot);

        return medicineLotMapper.toResponse(updatedMedicineLot);
    }

    @Override
    @Transactional
    public MedicineLotResponse deactivateMedicineLot(Long id) {
        MedicineLot medicineLot = findActiveMedicineLotById(id);

        medicineLot.setIsActive(0);
        MedicineLot deactivatedMedicineLot = medicineLotRepository.save(medicineLot);

        return medicineLotMapper.toResponse(deactivatedMedicineLot);
    }

    private MedicineLot findActiveMedicineLotById(Long id) {
        return medicineLotRepository.findByMedicineLotIdAndIsActive(id, 1)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine lot not found with id: " + id));
    }

    private Medicine findActiveMedicineById(Long medicineId) {
        return medicineRepository.findByMedicineIdAndIsActive(medicineId, 1)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with id: " + medicineId));
    }

    private void validateMedicineLotRequest(MedicineLotRequest request) {
        if (request.getQuantity() < 0) {
            throw new BusinessException("Quantity must be greater than or equal to 0");
        }

        if (request.getManufacturingDate() != null
                && !request.getExpiryDate().isAfter(request.getManufacturingDate())) {
            throw new BusinessException("Expiry date must be after manufacturing date");
        }
    }

    private void validateDuplicateLotNumber(Long medicineId, String lotNumber, Long currentId) {
        String normalizedLotNumber = lotNumber.trim();

        boolean duplicated = currentId == null
                ? medicineLotRepository.existsByMedicine_MedicineIdAndLotNumberIgnoreCase(medicineId, normalizedLotNumber)
                : medicineLotRepository.existsByMedicine_MedicineIdAndLotNumberIgnoreCaseAndMedicineLotIdNot(
                        medicineId,
                        normalizedLotNumber,
                        currentId
                );

        if (duplicated) {
            throw new DuplicateResourceException("Medicine lot number already exists for this medicine");
        }
    }
}