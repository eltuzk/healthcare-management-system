package com.healthcare.backend.service.impl;

import com.healthcare.backend.dto.request.ReceptionistRequest;
import com.healthcare.backend.dto.response.ReceptionistResponse;
import com.healthcare.backend.entity.Account;
import com.healthcare.backend.entity.Receptionist;
import com.healthcare.backend.mapper.ReceptionistMapper;
import com.healthcare.backend.repository.AccountRepository;
import com.healthcare.backend.repository.ReceptionistRepository;
import com.healthcare.backend.service.ReceptionistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReceptionistServiceImpl implements ReceptionistService {

    private final ReceptionistRepository receptionistRepository;
    private final AccountRepository accountRepository;
    private final ReceptionistMapper receptionistMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<ReceptionistResponse> getAll(Pageable pageable) {
        return receptionistRepository.findAll(pageable)
                .map(receptionistMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ReceptionistResponse getById(Long id) {
        Receptionist receptionist = receptionistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Receptionist not found with id: " + id));
        return receptionistMapper.toResponse(receptionist);
    }

    @Override
    @Transactional
    public ReceptionistResponse create(ReceptionistRequest request) {
        Receptionist receptionist = receptionistMapper.toEntity(request);

        if (request.getAccountId() != null) {
            Account account = accountRepository.findById(request.getAccountId())
                    .orElseThrow(() -> new RuntimeException("Account not found with id: " + request.getAccountId()));
            receptionist.setAccount(account);
        }

        Receptionist savedReceptionist = receptionistRepository.save(receptionist);
        return receptionistMapper.toResponse(savedReceptionist);
    }

    @Override
    @Transactional
    public ReceptionistResponse update(Long id, ReceptionistRequest request) {
        Receptionist existingReceptionist = receptionistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Receptionist not found with id: " + id));

        receptionistMapper.updateEntityFromRequest(request, existingReceptionist);
        
        Receptionist updatedReceptionist = receptionistRepository.save(existingReceptionist);
        return receptionistMapper.toResponse(updatedReceptionist);
    }

    @Override
    @Transactional(readOnly = true)
    public ReceptionistResponse getMe(String email) {
        return receptionistMapper.toResponse(receptionistRepository.findByAccount_Email(email)
                .orElseThrow(() -> new RuntimeException("Receptionist not found with email: " + email)));
    }

    @Override
    @Transactional
    public ReceptionistResponse updateMe(String email, ReceptionistRequest request) {
        Receptionist receptionist = receptionistRepository.findByAccount_Email(email)
                .orElseThrow(() -> new RuntimeException("Receptionist not found with email: " + email));
        
        if (request.getIdentityNum() != null 
                && !request.getIdentityNum().isBlank()
                && receptionistRepository.existsByIdentityNumAndReceptionistIdNot(request.getIdentityNum(), receptionist.getReceptionistId())) {
            throw new RuntimeException("Số CCCD đã tồn tại: " + request.getIdentityNum());
        }

        receptionistMapper.updateEntityFromRequest(request, receptionist);
        return receptionistMapper.toResponse(receptionistRepository.save(receptionist));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Receptionist receptionist = receptionistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Receptionist not found with id: " + id));
        
        receptionist.setIsActive(0);
        receptionistRepository.save(receptionist);
    }
}