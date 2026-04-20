package com.healthcare.backend.service.impl;

import com.healthcare.backend.dto.request.PatientInsuranceRequest;
import com.healthcare.backend.dto.response.PatientInsuranceResponse;
import com.healthcare.backend.entity.Patient;
import com.healthcare.backend.entity.PatientInsurance;
import com.healthcare.backend.exception.BusinessException;
import com.healthcare.backend.exception.DuplicateResourceException;
import com.healthcare.backend.exception.ResourceNotFoundException;
import com.healthcare.backend.mapper.PatientInsuranceMapper;
import com.healthcare.backend.repository.PatientInsuranceRepository;
import com.healthcare.backend.repository.PatientRepository;
import com.healthcare.backend.service.PatientInsuranceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientInsuranceServiceImpl implements PatientInsuranceService {

    private final PatientInsuranceRepository patientInsuranceRepository;
    private final PatientRepository patientRepository;
    private final PatientInsuranceMapper patientInsuranceMapper;

    @Override
    @Transactional(readOnly = true)
    public List<PatientInsuranceResponse> getByPatientId(Long patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Không tìm thấy bệnh nhân với id: " + patientId);
        }
        return patientInsuranceRepository.findAllByPatient_PatientId(patientId)
                .stream().map(patientInsuranceMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public PatientInsuranceResponse create(PatientInsuranceRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bệnh nhân với id: " + request.getPatientId()));

        if (!Integer.valueOf(1).equals(patient.getIsActive())) {
            throw new ResourceNotFoundException("Bệnh nhân không hoạt động với id: " + request.getPatientId());
        }

        if (patientInsuranceRepository.existsByInsuranceNum(request.getInsuranceNum())) {
            throw new DuplicateResourceException("Số bảo hiểm đã tồn tại: " + request.getInsuranceNum());
        }

        String status = (request.getStatus() != null) ? request.getStatus() : "ACTIVE";

        if ("ACTIVE".equals(status)
                && patientInsuranceRepository.existsByPatient_PatientIdAndStatus(request.getPatientId(), "ACTIVE")) {
            throw new BusinessException("Patient already has an active insurance");
        }

        PatientInsurance insurance = patientInsuranceMapper.toEntity(request);
        insurance.setStatus(status);
        insurance.setPatient(patient);

        return patientInsuranceMapper.toResponse(patientInsuranceRepository.save(insurance));
    }

    @Override
    @Transactional
    public PatientInsuranceResponse update(Long id, PatientInsuranceRequest request) {
        PatientInsurance insurance = findOrThrow(id);

        if (request.getInsuranceNum() != null
                && patientInsuranceRepository.existsByInsuranceNumAndPatientInsuranceIdNot(request.getInsuranceNum(), id)) {
            throw new DuplicateResourceException("Số bảo hiểm đã tồn tại: " + request.getInsuranceNum());
        }

        if ("ACTIVE".equals(request.getStatus()) && !"ACTIVE".equals(insurance.getStatus())) {
            Long patientId = insurance.getPatient().getPatientId();
            if (patientInsuranceRepository.existsByPatient_PatientIdAndStatus(patientId, "ACTIVE")) {
                throw new BusinessException("Patient already has an active insurance");
            }
        }

        patientInsuranceMapper.updateEntityFromRequest(request, insurance);

        return patientInsuranceMapper.toResponse(patientInsuranceRepository.save(insurance));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        findOrThrow(id);
        patientInsuranceRepository.deleteById(id);
    }

    private PatientInsurance findOrThrow(Long id) {
        return patientInsuranceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bảo hiểm với id: " + id));
    }
}
