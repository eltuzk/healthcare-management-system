package com.healthcare.backend.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.healthcare.backend.dto.request.DoctorRequestDTO;
import com.healthcare.backend.dto.response.DoctorResponseDTO;
import com.healthcare.backend.entity.Account;
import com.healthcare.backend.entity.Doctor;
import com.healthcare.backend.repository.AccountRepository;
import com.healthcare.backend.repository.DoctorRepository;
import com.healthcare.backend.service.DoctorServiceInterface;

import jakarta.annotation.Nullable;

@Service
public class DoctorServiceImpl implements DoctorServiceInterface {
    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Override
    public Page<DoctorResponseDTO> getAllDoctors(Pageable pageable, @Nullable String specialization) {
        return doctorRepository.findDoctorsBySpecialization(pageable, specialization).map(doctor -> new DoctorResponseDTO(
            doctor.getDoctorId(),
            doctor.getAccount().getEmail(),
            doctor.getFullName(),
            doctor.getSpecialization(),
            doctor.getLicenseNum(),
            doctor.getQualification(),
            doctor.getExperience(),
            doctor.getGender(),
            doctor.getPhone(),
            doctor.getAddress(),
            doctor.getHireDate(),
            doctor.getIdentityNum(),
            doctor.getDateOfBirth(),
            doctor.isActive()
        ));
    }

    @Override
    public DoctorResponseDTO getDoctorById(Long doctorId) {
        return doctorRepository.findById(doctorId)
        .map(doctor -> new DoctorResponseDTO(
            doctor.getDoctorId(),
            doctor.getAccount().getEmail(),
            doctor.getFullName(),
            doctor.getSpecialization(),
            doctor.getLicenseNum(),
            doctor.getQualification(),
            doctor.getExperience(),
            doctor.getGender(),
            doctor.getPhone(),
            doctor.getAddress(),
            doctor.getHireDate(),
            doctor.getIdentityNum(),
            doctor.getDateOfBirth(),
            doctor.isActive()
        ))
        .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + doctorId));
    }

    @Override
    public DoctorResponseDTO createDoctor(DoctorRequestDTO doctorRequest) {
        Account account = accountRepository.findByEmail(doctorRequest.getAccountEmail())
            .orElseThrow(() -> new RuntimeException("Account not found by this email."));
        if(doctorRepository.existsByAccount_Email(doctorRequest.getAccountEmail())) {
            throw new RuntimeException("This account being used.");
        }
        if(!account.getRole().getRoleName().equals("DOCTOR")) {
            throw new RuntimeException("This account invalid.");
        }

        if(doctorRepository.existsByLicenseNum(doctorRequest.getLicenseNum())) {
            throw new RuntimeException("This license number already exists.");
        }

        if(doctorRepository.existsByIdentityNum(doctorRequest.getIdentityNum())) {
            throw new RuntimeException("This identify number already exists.");
        }

        Doctor doctor = new Doctor();
        account.setActive(true);
        doctor.setAccount(account);
        doctor.setAddress(doctorRequest.getAddress());
        doctor.setDateOfBirth(doctorRequest.getDateOfBirth());
        doctor.setExperience(doctorRequest.getExperience());
        doctor.setFullName(doctorRequest.getFullName());
        doctor.setGender(doctorRequest.getGender());
        doctor.setHireDate(doctorRequest.getHireDate());
        doctor.setIdentityNum(doctorRequest.getIdentityNum());
        doctor.setLicenseNum(doctorRequest.getLicenseNum());
        doctor.setPhone(doctorRequest.getPhone());
        doctor.setQualification(doctorRequest.getQualification());
        doctor.setSpecialization(doctorRequest.getSpecialization());
        doctor.setActive(true);

        doctorRepository.save(doctor);

        return new DoctorResponseDTO(
            doctor.getDoctorId(),
            doctor.getAccount().getEmail(),
            doctor.getFullName(),
            doctor.getSpecialization(),
            doctor.getLicenseNum(),
            doctor.getQualification(),
            doctor.getExperience(),
            doctor.getGender(),
            doctor.getPhone(),
            doctor.getAddress(),
            doctor.getHireDate(),
            doctor.getIdentityNum(),
            doctor.getDateOfBirth(),
            doctor.isActive()
        );
    }

    @Override
    public DoctorResponseDTO updateDoctor(DoctorRequestDTO doctorRequest, Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
            .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + doctorId));
        
        String currentEmail = doctor.getAccount().getEmail();
        String newEmail = doctorRequest.getAccountEmail();
        
        if (newEmail != null && !newEmail.equals(currentEmail)) {
            if (doctorRepository.existsByAccount_Email(newEmail)) {
                throw new RuntimeException("Email being used by another.");
            }
            Account newAccount = accountRepository.findByEmail(newEmail)
                .orElseThrow(() -> new RuntimeException("Account not found with this email."));
            if (!newAccount.getRole().getRoleName().equals("DOCTOR")) {
                throw new RuntimeException("Account invalid.");
            }
            doctor.setAccount(newAccount);
        }

        doctor.setFullName(doctorRequest.getFullName());
        doctor.setSpecialization(doctorRequest.getSpecialization());

        String currentLicense = doctor.getLicenseNum();
        String newLicense = doctorRequest.getLicenseNum();
        if (newLicense != null && !newLicense.equals(currentLicense)) {
            if (doctorRepository.existsByLicenseNum(newLicense)) {
                throw new RuntimeException("This license number already exists.");
            }
            doctor.setLicenseNum(newLicense);
        }

        doctor.setLicenseNum(doctorRequest.getLicenseNum());
        doctor.setQualification(doctorRequest.getQualification());
        doctor.setExperience(doctorRequest.getExperience());
        doctor.setGender(doctorRequest.getGender());
        doctor.setPhone(doctorRequest.getPhone());
        doctor.setAddress(doctorRequest.getAddress());

        String currentIdentity = doctor.getIdentityNum();
        String newIdentity = doctorRequest.getIdentityNum();
        if (newIdentity != null && !newIdentity.equals(currentIdentity)) {
            if (doctorRepository.existsByIdentityNum(newIdentity)) {
                throw new RuntimeException("This identity number already exists.");
            }
            doctor.setIdentityNum(newIdentity);
        }
        doctor.setIdentityNum(doctorRequest.getIdentityNum());

        doctor.setDateOfBirth(doctorRequest.getDateOfBirth());
        doctor.setHireDate(doctorRequest.getHireDate());

        if (doctor.isActive() != doctorRequest.isStatus()) {
            doctor.setActive(doctorRequest.isStatus());
            
            if (!doctorRequest.isStatus()) {
                Account currentAccount = doctor.getAccount();
                currentAccount.setActive(false);
                accountRepository.save(currentAccount);
            }
        }

        doctorRepository.save(doctor);

        return new DoctorResponseDTO(
            doctor.getDoctorId(),
            doctor.getAccount().getEmail(),
            doctor.getFullName(),
            doctor.getSpecialization(),
            doctor.getLicenseNum(),
            doctor.getQualification(),
            doctor.getExperience(),
            doctor.getGender(),
            doctor.getPhone(),
            doctor.getAddress(),
            doctor.getHireDate(),
            doctor.getIdentityNum(),
            doctor.getDateOfBirth(),
            doctor.isActive()
        );
    }

    @Override
    public void deleteDoctor(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
            .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + doctorId));
        
        doctor.setActive(false);
        doctorRepository.save(doctor);
    }
}
