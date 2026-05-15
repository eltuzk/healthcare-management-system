package com.healthcare.backend.service.impl;

import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.healthcare.backend.dto.request.AccountRequest;
import com.healthcare.backend.dto.response.AccountResponse;
import com.healthcare.backend.entity.Account;
import com.healthcare.backend.entity.Accountant;
import com.healthcare.backend.entity.Doctor;
import com.healthcare.backend.entity.Administrator;
import com.healthcare.backend.entity.Pharmacist;
import com.healthcare.backend.entity.Receptionist;
import com.healthcare.backend.entity.Role;
import com.healthcare.backend.entity.Technician;
import com.healthcare.backend.exception.DuplicateResourceException;
import com.healthcare.backend.exception.ResourceNotFoundException;
import com.healthcare.backend.mapper.AccountMapper;
import com.healthcare.backend.repository.AccountRepository;
import com.healthcare.backend.repository.AccountantRepository;
import com.healthcare.backend.repository.AdministratorRepository;
import com.healthcare.backend.repository.DoctorRepository;
import com.healthcare.backend.repository.PatientRepository;
import com.healthcare.backend.repository.PharmacistRepository;
import com.healthcare.backend.repository.ReceptionistRepository;
import com.healthcare.backend.repository.RoleRepository;
import com.healthcare.backend.repository.TechnicianRepository;
import com.healthcare.backend.service.AccountService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final ReceptionistRepository receptionistRepository;
    private final PharmacistRepository pharmacistRepository;
    private final TechnicianRepository technicianRepository;
    private final AccountantRepository accountantRepository;
    private final AdministratorRepository administratorRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountMapper accountMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<AccountResponse> getAll(Pageable pageable) {
        return accountRepository.findAllByIsActive(1, pageable)
                .map(accountMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getById(Long id) {
        return accountMapper.toResponse(findOrThrow(id));
    }

    @Override
    @Transactional
    public AccountResponse create(AccountRequest request) {
        if (accountRepository.existsByEmail(request.getEmail()))
            throw new DuplicateResourceException("Email already exists: " + request.getEmail());

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + request.getRoleId()));

        Account account = accountMapper.toEntity(request);
        account.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        account.setRole(role);
        account.setIsActive(1);

        Account savedAccount = accountRepository.save(account);

        String roleName = role.getRoleName().toUpperCase();

        switch (roleName) {
            case "DOCTOR":
                Doctor doctor = new Doctor();
                doctor.setAccount(savedAccount);
                doctor.setFullName(request.getFullName() != null ? request.getFullName() : "Chưa cập nhật");
                doctor.setLicenseNum("PENDING-" + savedAccount.getAccountId()); // Xử lý cột Unique
                doctorRepository.save(doctor);
                break;
                
            case "RECEPTIONIST":
                Receptionist receptionist = new Receptionist();
                receptionist.setAccount(savedAccount);
                receptionist.setFullName(request.getFullName() != null ? request.getFullName() : "Chưa cập nhật");
                receptionistRepository.save(receptionist);
                break;
                
            case "PHARMACIST":
                Pharmacist pharmacist = new Pharmacist();
                pharmacist.setAccount(savedAccount);
                pharmacist.setFullName(request.getFullName() != null ? request.getFullName() : "Chưa cập nhật");
                pharmacist.setLicenseNum("PENDING-" + savedAccount.getAccountId());
                pharmacistRepository.save(pharmacist);
                break;
                
            case "TECHNICIAN":
                Technician technician = new Technician();
                technician.setAccount(savedAccount);
                technician.setFullName(request.getFullName() != null ? request.getFullName() : "Chưa cập nhật");
                technicianRepository.save(technician);
                break;
                
            case "ACCOUNTANT":
                Accountant accountant = new Accountant();
                accountant.setAccount(savedAccount);
                accountant.setFullName(request.getFullName() != null ? request.getFullName() : "Chưa cập nhật");
                accountantRepository.save(accountant);
                break;
                
            case "ADMIN":
                Administrator admin = new Administrator();
                admin.setAccount(savedAccount);
                admin.setFullName(request.getFullName() != null ? request.getFullName() : "Quản trị viên");
                administratorRepository.save(admin);
                break;
                
            default:
                break;
        }

        return accountMapper.toResponse(savedAccount);
    }

    @Override
    public AccountResponse update(Long id, AccountRequest request) {
        Account account = findOrThrow(id);

        if (request.getEmail() != null && !request.getEmail().equals(account.getEmail())) {
            if (accountRepository.existsByEmail(request.getEmail()))
                throw new DuplicateResourceException("Email already exists: " + request.getEmail());
            accountMapper.updateEntityFromRequest(request, account);
        }

        if (request.getRoleId() != null && !request.getRoleId().equals(account.getRole().getRoleId())) {
            Role role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + request.getRoleId()));
            account.setRole(role);
        }

        return accountMapper.toResponse(accountRepository.save(account));
    }

    @Override
    public void delete(Long id) {
        Account account = findOrThrow(id);
        account.setIsActive(0);
        accountRepository.save(account);

        doctorRepository.findByAccount_AccountId(id).ifPresent(doctor -> {
            doctor.setActive(false);
            doctorRepository.save(doctor);
        });

        patientRepository.findByAccount_AccountId(id).ifPresent(patient -> {
            patient.setIsActive(0);
            patientRepository.save(patient);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getMe(String email) {
        Account account = accountRepository.findByEmailAndIsActive(email, 1)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + email));
        return accountMapper.toResponse(account);
    }

    private Account findOrThrow(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + id));
        if (!Objects.equals(account.getIsActive(), 1))
            throw new ResourceNotFoundException("Account not found: " + id);
        return account;
    }
}
