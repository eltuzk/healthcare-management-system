package com.healthcare.backend.repository;

import com.healthcare.backend.entity.ConsultationFee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface ConsultationFeeRepository extends JpaRepository<ConsultationFee, Long> {

    Optional<ConsultationFee> findFirstBySpecialtyRef_SpecialtyIdAndIsActive(Long specialtyId, Integer isActive);

    List<ConsultationFee> findAllByOrderBySpecialtyAscFeeNameAsc();

    boolean existsByFeeCodeIgnoreCase(String feeCode);

    boolean existsByFeeCodeIgnoreCaseAndFeeIdNot(String feeCode, Long feeId);

    boolean existsBySpecialtyRef_SpecialtyId(Long specialtyId);

    boolean existsBySpecialtyRef_SpecialtyIdAndFeeIdNot(Long specialtyId, Long feeId);
}
