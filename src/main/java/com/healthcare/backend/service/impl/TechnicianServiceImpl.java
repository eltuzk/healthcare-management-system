package com.healthcare.backend.service.impl;

import com.healthcare.backend.dto.request.TechnicianRequest;
import com.healthcare.backend.dto.response.TechnicianResponse;
import com.healthcare.backend.entity.Account;
import com.healthcare.backend.entity.Technician;
import com.healthcare.backend.mapper.TechnicianMapper;
import com.healthcare.backend.repository.AccountRepository;
import com.healthcare.backend.repository.TechnicianRepository;
import com.healthcare.backend.service.TechnicianService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TechnicianServiceImpl implements TechnicianService {

    private final TechnicianRepository technicianRepository;
    private final AccountRepository accountRepository;
    private final TechnicianMapper technicianMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<TechnicianResponse> getAll(Pageable pageable) {
        return technicianRepository.findAll(pageable)
                .map(technicianMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public TechnicianResponse getById(Long id) {
        Technician technician = technicianRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Technician not found with id: " + id));
        return technicianMapper.toResponse(technician);
    }

    @Override
    @Transactional
    public TechnicianResponse create(TechnicianRequest request) {
        Technician technician = technicianMapper.toEntity(request);

        if (request.getAccountId() != null) {
            Account account = accountRepository.findById(request.getAccountId())
                    .orElseThrow(() -> new RuntimeException("Account not found with id: " + request.getAccountId()));
            technician.setAccount(account);
        }

        Technician savedTechnician = technicianRepository.save(technician);
        return technicianMapper.toResponse(savedTechnician);
    }

    @Override
    @Transactional
    public TechnicianResponse update(Long id, TechnicianRequest request) {
        Technician existingTechnician = technicianRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Technician not found with id: " + id));

        technicianMapper.updateEntityFromRequest(request, existingTechnician);
        
        Technician updatedTechnician = technicianRepository.save(existingTechnician);
        return technicianMapper.toResponse(updatedTechnician);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Technician technician = technicianRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Technician not found with id: " + id));
        
        technician.setIsActive(0); 
        technicianRepository.save(technician);
    }
}