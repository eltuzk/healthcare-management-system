package com.healthcare.backend.mapper;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import com.healthcare.backend.repository.DoctorRepository;
import com.healthcare.backend.repository.PatientRepository;
import com.healthcare.backend.repository.ReceptionistRepository;
import com.healthcare.backend.repository.PharmacistRepository;
import com.healthcare.backend.repository.TechnicianRepository;
import com.healthcare.backend.repository.AccountantRepository;
import com.healthcare.backend.dto.request.AccountRequest;
import com.healthcare.backend.dto.response.AccountResponse;
import com.healthcare.backend.entity.Account;

@Component
@RequiredArgsConstructor
public class AccountMapper {

    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final ReceptionistRepository receptionistRepository;
    private final PharmacistRepository pharmacistRepository;
    private final TechnicianRepository technicianRepository;
    private final AccountantRepository accountantRepository;

    public Account toEntity(AccountRequest request) {
        Account entity = new Account();
        entity.setEmail(request.getEmail());
        return entity;
    }

    public AccountResponse toResponse(Account entity) {
        AccountResponse response = new AccountResponse();
        response.setAccountId(entity.getAccountId());
        response.setEmail(entity.getEmail());
        if (entity.getRole() != null) {
            response.setRoleId(entity.getRole().getRoleId());
            response.setRoleName(entity.getRole().getRoleName());
        }
        response.setIsActive(entity.getIsActive());

        // Fetch fullName from actor tables
        if (entity.getRole() != null) {
            String roleName = entity.getRole().getRoleName();
            String fullName = "Chưa cập nhật";
            
            switch (roleName) {
                case "DOCTOR":
                    fullName = doctorRepository.findByAccount_AccountId(entity.getAccountId())
                            .map(d -> d.getFullName()).orElse(fullName);
                    break;
                case "PATIENT":
                    fullName = patientRepository.findByAccount_AccountId(entity.getAccountId())
                            .map(p -> p.getFullName()).orElse(fullName);
                    break;
                case "RECEPTIONIST":
                    fullName = receptionistRepository.findByAccount_AccountId(entity.getAccountId())
                            .map(r -> r.getFullName()).orElse(fullName);
                    break;
                case "PHARMACIST":
                    fullName = pharmacistRepository.findByAccount_AccountId(entity.getAccountId())
                            .map(p -> p.getFullName()).orElse(fullName);
                    break;
                case "TECHNICIAN":
                    fullName = technicianRepository.findByAccount_AccountId(entity.getAccountId())
                            .map(t -> t.getFullName()).orElse(fullName);
                    break;
                case "ACCOUNTANT":
                    fullName = accountantRepository.findByAccount_AccountId(entity.getAccountId())
                            .map(a -> a.getFullName()).orElse(fullName);
                    break;
            }
            response.setFullName(fullName);
        }

        return response;
    }

    public void updateEntityFromRequest(AccountRequest request, Account entity) {
        if (request.getEmail() != null) entity.setEmail(request.getEmail());
    }
}
