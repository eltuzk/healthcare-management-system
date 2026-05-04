package com.healthcare.backend.service.impl;

import com.healthcare.backend.dto.request.ConsultationFeeRequest;
import com.healthcare.backend.dto.response.ConsultationFeeResponse;
import com.healthcare.backend.entity.ConsultationFee;
import com.healthcare.backend.entity.Specialty;
import com.healthcare.backend.exception.DuplicateResourceException;
import com.healthcare.backend.exception.ResourceNotFoundException;
import com.healthcare.backend.mapper.ConsultationFeeMapper;
import com.healthcare.backend.repository.ConsultationFeeRepository;
import com.healthcare.backend.repository.SpecialtyRepository;
import com.healthcare.backend.service.ConsultationFeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultationFeeServiceImpl implements ConsultationFeeService {

    private final ConsultationFeeRepository consultationFeeRepository;
    private final SpecialtyRepository specialtyRepository;
    private final ConsultationFeeMapper consultationFeeMapper;

    @Override
    // Transaction đảm bảo phí khám và mapping chuyên khoa được lưu cùng nhau, tránh fee thiếu specialty_id.
    @Transactional
    public ConsultationFeeResponse create(ConsultationFeeRequest request) {
        Specialty specialty = findActiveSpecialtyOrThrow(request.getSpecialtyId());
        validateDuplicates(request, null);

        ConsultationFee consultationFee = consultationFeeMapper.toEntity(request);
        setSpecialty(consultationFee, specialty);
        return consultationFeeMapper.toResponse(consultationFeeRepository.save(consultationFee));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsultationFeeResponse> getAll() {
        return consultationFeeRepository.findAllByOrderBySpecialtyAscFeeNameAsc()
                .stream()
                .map(consultationFeeMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ConsultationFeeResponse getById(Long feeId) {
        return consultationFeeMapper.toResponse(findOrThrow(feeId));
    }

    @Override
    // Transaction đảm bảo khi đổi chuyên khoa/giá thì cả FK specialty_id và text legacy specialty được đồng bộ.
    @Transactional
    public ConsultationFeeResponse update(Long feeId, ConsultationFeeRequest request) {
        ConsultationFee consultationFee = findOrThrow(feeId);
        Specialty specialty = findActiveSpecialtyOrThrow(request.getSpecialtyId());
        validateDuplicates(request, feeId);

        consultationFeeMapper.updateEntityFromRequest(request, consultationFee);
        setSpecialty(consultationFee, specialty);
        return consultationFeeMapper.toResponse(consultationFeeRepository.save(consultationFee));
    }

    @Override
    @Transactional
    public ConsultationFeeResponse deactivate(Long feeId) {
        ConsultationFee consultationFee = findOrThrow(feeId);
        consultationFee.setIsActive(0);
        return consultationFeeMapper.toResponse(consultationFeeRepository.save(consultationFee));
    }

    private ConsultationFee findOrThrow(Long feeId) {
        return consultationFeeRepository.findById(feeId)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation fee not found with id: " + feeId));
    }

    private void validateDuplicates(ConsultationFeeRequest request, Long feeId) {
        String feeCode = request.getFeeCode().trim();

        boolean duplicatedFeeCode = feeId == null
                ? consultationFeeRepository.existsByFeeCodeIgnoreCase(feeCode)
                : consultationFeeRepository.existsByFeeCodeIgnoreCaseAndFeeIdNot(feeCode, feeId);
        if (duplicatedFeeCode) {
            throw new DuplicateResourceException("Consultation fee code already exists");
        }

        boolean duplicatedSpecialty = feeId == null
                ? consultationFeeRepository.existsBySpecialtyRef_SpecialtyId(request.getSpecialtyId())
                : consultationFeeRepository.existsBySpecialtyRef_SpecialtyIdAndFeeIdNot(request.getSpecialtyId(), feeId);
        if (duplicatedSpecialty) {
            throw new DuplicateResourceException("Consultation fee specialty already exists");
        }
    }

    private Specialty findActiveSpecialtyOrThrow(Long specialtyId) {
        Specialty specialty = specialtyRepository.findById(specialtyId)
                .orElseThrow(() -> new ResourceNotFoundException("Specialty not found with id: " + specialtyId));
        if (!specialty.isActive()) {
            throw new ResourceNotFoundException("Specialty not found with id: " + specialtyId);
        }
        return specialty;
    }

    private void setSpecialty(ConsultationFee consultationFee, Specialty specialty) {
        // specialtyRef là FK chuẩn để tính giá; specialty text chỉ giữ lại để tương thích dữ liệu legacy/hiển thị.
        consultationFee.setSpecialtyRef(specialty);
        consultationFee.setSpecialty(specialty.getSpecialtyName());
    }
}
