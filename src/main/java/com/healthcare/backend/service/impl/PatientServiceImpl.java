package com.healthcare.backend.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import com.healthcare.backend.dto.request.PatientRequest;
import com.healthcare.backend.dto.response.PatientResponse;
import com.healthcare.backend.entity.Account;
import com.healthcare.backend.entity.Patient;
import com.healthcare.backend.mapper.PatientMapper;
import com.healthcare.backend.repository.AccountRepository;
import com.healthcare.backend.repository.PatientRepository;
import com.healthcare.backend.service.PatientService;

@RestController
public class PatientServiceImpl implements PatientService {
    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PatientMapper patientMapper;

    @Override
    public Page<PatientResponse> getAllPatients(Pageable pageable) {
        return patientRepository.findAll(pageable).map(patientMapper::toDto);
    }

    @Override
    public PatientResponse getPatientById(Long patientId) {
        return patientRepository.findById(patientId)
            .map(patientMapper::toDto)
            .orElseThrow(() -> new RuntimeException("Patient not found with: " + patientId));
    }

    @Override
    public PatientResponse createPatient(PatientRequest patientRequest) {
        if(patientRepository.existsByPhone(patientRequest.getPhone())) {
            throw new RuntimeException("This number phone already exists.");
        }

        if(patientRepository.existsByIdentityNum(patientRequest.getIdentityNum())) {
            throw new RuntimeException("This identity number already exists.");
        }

        Patient newPatient = patientMapper.createEntityFromDto(patientRequest);

        String email = patientRequest.getAccountEmail();
        if(email != null) {
            Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Account not found."));
            
            if (!"PATIENT".equals(account.getRole().getRoleName())) {
                throw new RuntimeException("This account is not a PATIENT role.");
            }

            if(patientRepository.existsByAccount_Email(email)) {
                throw new RuntimeException("This account is already linked.");
            }

            newPatient.setAccount(account);
        }

        patientRepository.save(newPatient);
        return patientMapper.toDto(newPatient); 
    }

    @Override
    public PatientResponse updatePatient(Long patientId, PatientRequest patientRequest) {
        Patient patient = patientRepository.findById(patientId).orElse(null);
        if(patient == null) {
            throw new RuntimeException("Patient not found with id: " + patientId);
        }

        String newEmail = null;
        if (patient.getAccount() != null) {
            newEmail = patient.getAccount().getEmail();
        }
        if (newEmail != null && !newEmail.equals(patient.getAccount().getEmail())) {
            Account newAccount = accountRepository.findByEmail(newEmail)
                    .orElseThrow(() -> new RuntimeException("New account not found."));
            
            if (patientRepository.existsByAccount_Email(newEmail)) {
                throw new RuntimeException("The new account is already linked to another doctor.");
            }
            patient.setAccount(newAccount);
        }

        String newIdentity = patientRequest.getIdentityNum();
        if (newIdentity != null && !newIdentity.equals(patient.getIdentityNum())) {
            if (patientRepository.existsByIdentityNum(newIdentity)) {
                throw new RuntimeException("This identity number already exists.");
            }
        }

        patientMapper.updatePatientFromDto(patient, patientRequest);

        patientRepository.save(patient);
        return patientMapper.toDto(patient); 
    }

    @Override
    public void deletePatient(Long patientId) {
        Patient patient = patientRepository.findById(patientId).orElse(null);
        if(patient == null) throw new RuntimeException("Patient not found with id: " + patientId);

        patient.setIsActive(false);

        if(patient.getAccount() != null) {
            
        }
        
        patientRepository.save(patient);
    }

}
