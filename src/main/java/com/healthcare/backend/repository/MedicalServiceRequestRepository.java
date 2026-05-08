package com.healthcare.backend.repository;

import com.healthcare.backend.entity.MedicalServiceRequest;
import com.healthcare.backend.entity.enums.MedicalServiceRequestStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface MedicalServiceRequestRepository extends JpaRepository<MedicalServiceRequest, Long> {

    List<MedicalServiceRequest> findByMedRecord_MedicalRecordId(Long medicalRecordId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select msr
            from MedicalServiceRequest msr
            where msr.medServiceRequestId = :medicalServiceRequestId
            """)
    Optional<MedicalServiceRequest> findByIdForUpdate(@Param("medicalServiceRequestId") Long medicalServiceRequestId);

    Page<MedicalServiceRequest> findByMedRecord_MedicalRecordId(Long medicalRecordId, Pageable pageable);

    Page<MedicalServiceRequest> findByStatus(MedicalServiceRequestStatus status, Pageable pageable);

    Page<MedicalServiceRequest> findByMedRecord_MedicalRecordIdAndStatus(Long medicalRecordId, MedicalServiceRequestStatus status, Pageable pageable);

    long countByMedRecord_MedicalRecordId(Long medicalRecordId);

    long countByMedRecord_MedicalRecordIdAndStatusNot(Long medicalRecordId, MedicalServiceRequestStatus status);

    @Query("""
            select coalesce(sum(msr.totalPrice), 0)
            from MedicalServiceRequest msr
            where msr.medRecord.medicalRecordId = :medicalRecordId
            """)
    BigDecimal sumTotalPriceByMedicalRecordId(@Param("medicalRecordId") Long medicalRecordId);
}
