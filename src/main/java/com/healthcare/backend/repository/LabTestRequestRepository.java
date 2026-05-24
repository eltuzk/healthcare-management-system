package com.healthcare.backend.repository;

import com.healthcare.backend.entity.LabTestRequest;
import com.healthcare.backend.entity.enums.LabTestRequestStatus;
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

@Repository
public interface LabTestRequestRepository extends JpaRepository<LabTestRequest, Long> {

    List<LabTestRequest> findByMedRecord_MedicalRecordId(Long medRecordId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select ltr
            from LabTestRequest ltr
            where ltr.labTestRequestId = :labTestRequestId
            """)
    java.util.Optional<LabTestRequest> findByIdForUpdate(@Param("labTestRequestId") Long labTestRequestId);

    Page<LabTestRequest> findByStatus(LabTestRequestStatus status, Pageable pageable);

    Page<LabTestRequest> findByPaymentStatus(com.healthcare.backend.entity.enums.PaymentStatus paymentStatus, Pageable pageable);

    Page<LabTestRequest> findByStatusAndPaymentStatus(LabTestRequestStatus status, com.healthcare.backend.entity.enums.PaymentStatus paymentStatus, Pageable pageable);

    Page<LabTestRequest> findByMedRecord_MedicalRecordIdAndStatus(Long medRecordId, LabTestRequestStatus status, Pageable pageable);
    
    Page<LabTestRequest> findByMedRecord_MedicalRecordId(Long medRecordId, Pageable pageable);

    long countByMedRecord_MedicalRecordId(Long medRecordId);

    long countByMedRecord_MedicalRecordIdAndStatusNot(Long medRecordId, LabTestRequestStatus status);

    @Query("""
            select coalesce(sum(ltr.totalPrice), 0)
            from LabTestRequest ltr
            where ltr.medRecord.medicalRecordId = :medicalRecordId
            """)
    BigDecimal sumTotalPriceByMedicalRecordId(@Param("medicalRecordId") Long medicalRecordId);
}
