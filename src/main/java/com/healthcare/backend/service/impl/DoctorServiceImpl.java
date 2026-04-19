package com.healthcare.backend.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.healthcare.backend.dto.request.DoctorRequest;
import com.healthcare.backend.dto.response.DoctorResponse;
import com.healthcare.backend.entity.Account;
import com.healthcare.backend.entity.Doctor;
import com.healthcare.backend.mapper.DoctorMapper;
import com.healthcare.backend.repository.AccountRepository;
import com.healthcare.backend.repository.DoctorRepository;
import com.healthcare.backend.service.DoctorService;

import jakarta.annotation.Nullable;

@Service
public class DoctorServiceImpl implements DoctorService {
    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private DoctorMapper doctorMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<DoctorResponse> getAllDoctors(Pageable pageable, @Nullable String specialization) {
        return doctorRepository.findDoctorsBySpecialization(pageable, specialization).map(doctorMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorResponse getDoctorById(Long doctorId) {
        return doctorRepository.findById(doctorId)
        .map(doctorMapper::toDto)
        .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + doctorId));
    }

    @Override
    @Transactional
    public DoctorResponse createDoctor(DoctorRequest doctorRequest) {
        Account account = accountRepository.findByEmail(doctorRequest.getAccountEmail())
            .orElseThrow(() -> new RuntimeException("Account not found by this email."));
        if(doctorRepository.existsByAccount_Email(doctorRequest.getAccountEmail())) {
            throw new RuntimeException("This account being used.");
        }
        if(!"DOCTOR".equals(account.getRole().getRoleName())) {
            throw new RuntimeException("This account invalid.");
        }

        if(doctorRepository.existsByLicenseNum(doctorRequest.getLicenseNum())) {
            throw new RuntimeException("This license number already exists.");
        }

        if(doctorRepository.existsByIdentityNum(doctorRequest.getIdentityNum())) {
            throw new RuntimeException("This identify number already exists.");
        }

        Doctor doctor = doctorMapper.createEntityFromDto(doctorRequest);
        doctor.setAccount(account);

        doctorRepository.save(doctor);
        return doctorMapper.toDto(doctor);
    }

    @Override
    public DoctorResponse updateDoctor(DoctorRequest doctorRequest, Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
            .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + doctorId));
        
        String newEmail = doctorRequest.getAccountEmail();
        if (newEmail != null && !newEmail.equals(doctor.getAccount().getEmail())) {
            Account newAccount = accountRepository.findByEmail(newEmail)
                    .orElseThrow(() -> new RuntimeException("New account not found."));
            
            if (doctorRepository.existsByAccount_Email(newEmail)) {
                throw new RuntimeException("The new account is already linked to another doctor.");
            }
            doctor.setAccount(newAccount);
        }

        String newLicense = doctorRequest.getLicenseNum();
        if (newLicense != null && !newLicense.equals(doctor.getLicenseNum())) {
            if (doctorRepository.existsByLicenseNum(newLicense)) {
                throw new RuntimeException("This license number already exists.");
            }
        }

        String newIdentity = doctorRequest.getIdentityNum();
        if (newIdentity != null && !newIdentity.equals(doctor.getIdentityNum())) {
            if (doctorRepository.existsByIdentityNum(newIdentity)) {
                throw new RuntimeException("This identity number already exists.");
            }
        }

        doctorMapper.updateEntityFromDto(doctor, doctorRequest);

        doctor.getAccount().setActive(doctor.isActive());

        doctorRepository.save(doctor);
        return doctorMapper.toDto(doctor);
    }

    @Override
    @Transactional
    public void deleteDoctor(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + doctorId));
        
        doctor.setActive(false);
        doctor.getAccount().setActive(false);
        doctorRepository.save(doctor);
    }
}
