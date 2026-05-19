package com.healthcare.backend.service.impl;

import com.healthcare.backend.dto.request.PharmacistRequest;
import com.healthcare.backend.dto.response.PharmacistResponse;
import com.healthcare.backend.entity.Account;
import com.healthcare.backend.entity.Pharmacist;
import com.healthcare.backend.mapper.PharmacistMapper;
import com.healthcare.backend.repository.AccountRepository;
import com.healthcare.backend.repository.PharmacistRepository;
import com.healthcare.backend.service.PharmacistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PharmacistServiceImpl implements PharmacistService {

    private final PharmacistRepository pharmacistRepository;
    private final AccountRepository accountRepository;
    private final PharmacistMapper pharmacistMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<PharmacistResponse> getAll(Pageable pageable) {
        return pharmacistRepository.findAll(pageable)
                .map(pharmacistMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PharmacistResponse getById(Long id) {
        Pharmacist pharmacist = pharmacistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pharmacist not found with id: " + id));
        return pharmacistMapper.toResponse(pharmacist);
    }

    @Override
    @Transactional
    public PharmacistResponse create(PharmacistRequest request) {
        Pharmacist pharmacist = pharmacistMapper.toEntity(request);

        if (request.getAccountId() != null) {
            Account account = accountRepository.findById(request.getAccountId())
                    .orElseThrow(() -> new RuntimeException("Account not found with id: " + request.getAccountId()));
            pharmacist.setAccount(account);
        }

        Pharmacist savedPharmacist = pharmacistRepository.save(pharmacist);
        return pharmacistMapper.toResponse(savedPharmacist);
    }

    @Override
    @Transactional
    public PharmacistResponse update(Long id, PharmacistRequest request) {
        Pharmacist existingPharmacist = pharmacistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pharmacist not found with id: " + id));

        pharmacistMapper.updateEntityFromRequest(request, existingPharmacist);
        
        Pharmacist updatedPharmacist = pharmacistRepository.save(existingPharmacist);
        return pharmacistMapper.toResponse(updatedPharmacist);
    }

    @Override
    @Transactional(readOnly = true)
    public PharmacistResponse getMe(String email) {
        return pharmacistMapper.toResponse(pharmacistRepository.findByAccount_Email(email)
                .orElseThrow(() -> new RuntimeException("Pharmacist not found with email: " + email)));
    }

    @Override
    @Transactional
    public PharmacistResponse updateMe(String email, PharmacistRequest request) {
        Pharmacist pharmacist = pharmacistRepository.findByAccount_Email(email)
                .orElseThrow(() -> new RuntimeException("Pharmacist not found with email: " + email));
        
        if (request.getIdentityNum() != null 
                && !request.getIdentityNum().isBlank()
                && pharmacistRepository.existsByIdentityNumAndPharmacistIdNot(request.getIdentityNum(), pharmacist.getPharmacistId())) {
            throw new RuntimeException("Số CCCD đã tồn tại: " + request.getIdentityNum());
        }

        if (request.getLicenseNum() != null 
                && !request.getLicenseNum().isBlank()
                && pharmacistRepository.existsByLicenseNumAndPharmacistIdNot(request.getLicenseNum(), pharmacist.getPharmacistId())) {
            throw new RuntimeException("Số chứng chỉ hành nghề đã tồn tại: " + request.getLicenseNum());
        }

        pharmacistMapper.updateEntityFromRequest(request, pharmacist);
        return pharmacistMapper.toResponse(pharmacistRepository.save(pharmacist));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Pharmacist pharmacist = pharmacistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pharmacist not found with id: " + id));
        
        pharmacist.setIsActive(0); 
        pharmacistRepository.save(pharmacist);
    }
}