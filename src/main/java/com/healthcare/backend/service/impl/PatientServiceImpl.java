package com.healthcare.backend.service.impl;

import com.healthcare.backend.dto.request.PatientRequest;
import com.healthcare.backend.dto.response.PatientResponse;
import com.healthcare.backend.entity.Account;
import com.healthcare.backend.entity.Patient;
import com.healthcare.backend.exception.DuplicateResourceException;
import com.healthcare.backend.exception.ResourceNotFoundException;
import com.healthcare.backend.mapper.PatientMapper;
import com.healthcare.backend.repository.AccountRepository;
import com.healthcare.backend.repository.PatientRepository;
import com.healthcare.backend.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final AccountRepository accountRepository;
    private final PatientMapper patientMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<PatientResponse> getAll(Pageable pageable) {
        return patientRepository.findAllByIsActive(1, pageable).map(patientMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientResponse getById(Long id) {
        return patientMapper.toResponse(findOrThrow(id));
    }

    @Override
    @Transactional
    public PatientResponse create(PatientRequest request) {
        Account account = null;
        if (request.getAccountId() != null) {
            account = accountRepository.findById(request.getAccountId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với id: " + request.getAccountId()));

            if (!Integer.valueOf(1).equals(account.getIsActive())) {
                throw new ResourceNotFoundException("Tài khoản không hoạt động với id: " + request.getAccountId());
            }

            if (patientRepository.existsByAccount_AccountId(request.getAccountId())) {
                throw new DuplicateResourceException("Tài khoản đã được liên kết với bệnh nhân khác");
            }
        }

        if (request.getIdentityNum() != null && patientRepository.existsByIdentityNum(request.getIdentityNum())) {
            throw new DuplicateResourceException("Số CCCD đã tồn tại: " + request.getIdentityNum());
        }

        Patient patient = patientMapper.toEntity(request);
        patient.setIsActive(1);
        patient.setAccount(account);

        return patientMapper.toResponse(patientRepository.save(patient));
    }

    @Override
    @Transactional
    public PatientResponse update(Long id, PatientRequest request) {
        Patient patient = findOrThrow(id);

        if (request.getIdentityNum() != null
                && patientRepository.existsByIdentityNumAndPatientIdNot(request.getIdentityNum(), id)) {
            throw new DuplicateResourceException("Số CCCD đã tồn tại: " + request.getIdentityNum());
        }

        patientMapper.updateEntityFromRequest(request, patient);

        return patientMapper.toResponse(patientRepository.save(patient));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Patient patient = findOrThrow(id);
        patient.setIsActive(0);
        patientRepository.save(patient);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientResponse getMe(String email) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với email: " + email));

        Patient patient = patientRepository.findByAccount_AccountId(account.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ bệnh nhân cho tài khoản: " + email));

        return patientMapper.toResponse(patient);
    }

    private Patient findOrThrow(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bệnh nhân với id: " + id));
        if (!Integer.valueOf(1).equals(patient.getIsActive())) {
            throw new ResourceNotFoundException("Không tìm thấy bệnh nhân với id: " + id);
        }
        return patient;
    }
}
