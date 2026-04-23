package com.healthcare.backend.service.impl;

import com.healthcare.backend.dto.request.DoctorRequest;
import com.healthcare.backend.dto.response.DoctorResponse;
import com.healthcare.backend.entity.Account;
import com.healthcare.backend.entity.Doctor;
import com.healthcare.backend.entity.Specialty;
import com.healthcare.backend.exception.DuplicateResourceException;
import com.healthcare.backend.exception.ResourceNotFoundException;
import com.healthcare.backend.mapper.DoctorMapper;
import com.healthcare.backend.repository.AccountRepository;
import com.healthcare.backend.repository.DoctorRepository;
import com.healthcare.backend.repository.SpecialtyRepository;
import com.healthcare.backend.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;
    private final AccountRepository accountRepository;
    private final SpecialtyRepository specialtyRepository;
    private final DoctorMapper doctorMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<DoctorResponse> getAll(Pageable pageable) {
        return doctorRepository.findAllByIsActive(true, pageable).map(doctorMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorResponse getById(Long doctorId) {
        return doctorMapper.toResponse(findOrThrow(doctorId));
    }

    @Override
    @Transactional
    public DoctorResponse create(DoctorRequest request) {
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với id: " + request.getAccountId()));

        if (!Integer.valueOf(1).equals(account.getIsActive())) {
            throw new ResourceNotFoundException("Tài khoản không hoạt động với id: " + request.getAccountId());
        }

        if (doctorRepository.existsByAccount_AccountId(request.getAccountId())) {
            throw new DuplicateResourceException("Account đã được liên kết với bác sĩ khác");
        }

        if (doctorRepository.existsByLicenseNum(request.getLicenseNum())) {
            throw new DuplicateResourceException("Số giấy phép hành nghề đã tồn tại: " + request.getLicenseNum());
        }

        if (request.getIdentityNum() != null && doctorRepository.existsByIdentityNum(request.getIdentityNum())) {
            throw new DuplicateResourceException("Số CCCD đã tồn tại: " + request.getIdentityNum());
        }

        Specialty specialty = findActiveSpecialtyOrThrow(request.getSpecialtyId());
        Doctor doctor = doctorMapper.toEntity(request);
        doctor.setAccount(account);
        doctor.setSpecialty(specialty);
        doctor.setSpecialization(specialty.getSpecialtyName());
        doctor.setActive(true);

        return doctorMapper.toResponse(doctorRepository.save(doctor));
    }

    @Override
    @Transactional
    public DoctorResponse update(Long doctorId, DoctorRequest request) {
        Doctor doctor = findOrThrow(doctorId);
        Specialty specialty = findActiveSpecialtyOrThrow(request.getSpecialtyId());

        if (doctorRepository.existsByLicenseNumAndDoctorIdNot(request.getLicenseNum(), doctorId)) {
            throw new DuplicateResourceException("Số giấy phép hành nghề đã tồn tại: " + request.getLicenseNum());
        }

        if (request.getIdentityNum() != null
                && doctorRepository.existsByIdentityNumAndDoctorIdNot(request.getIdentityNum(), doctorId)) {
            throw new DuplicateResourceException("Số CCCD đã tồn tại: " + request.getIdentityNum());
        }

        doctorMapper.updateEntityFromRequest(request, doctor);
        doctor.setSpecialty(specialty);
        doctor.setSpecialization(specialty.getSpecialtyName());

        return doctorMapper.toResponse(doctorRepository.save(doctor));
    }

    @Override
    @Transactional
    public void delete(Long doctorId) {
        Doctor doctor = findOrThrow(doctorId);
        doctor.setActive(false);
        doctorRepository.save(doctor);
    }

    private Doctor findOrThrow(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bác sĩ với id: " + doctorId));
        if (!doctor.isActive()) {
            throw new ResourceNotFoundException("Không tìm thấy bác sĩ với id: " + doctorId);
        }
        return doctor;
    }

    private Specialty findActiveSpecialtyOrThrow(Long specialtyId) {
        Specialty specialty = specialtyRepository.findById(specialtyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyên khoa với id: " + specialtyId));
        if (!specialty.isActive()) {
            throw new ResourceNotFoundException("Không tìm thấy chuyên khoa với id: " + specialtyId);
        }
        return specialty;
    }
}
