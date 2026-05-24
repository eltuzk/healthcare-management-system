package com.healthcare.backend.repository;

import com.healthcare.backend.entity.MedicineLot;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicineLotRepository extends JpaRepository<MedicineLot, Long> {

    List<MedicineLot> findAllByIsActiveOrderByExpiryDateAsc(Integer isActive);

    List<MedicineLot> findAllByMedicine_MedicineIdAndIsActiveOrderByExpiryDateAsc(Long medicineId, Integer isActive);

    Optional<MedicineLot> findByMedicineLotIdAndIsActive(Long medicineLotId, Integer isActive);

    boolean existsByMedicine_MedicineIdAndLotNumberIgnoreCase(Long medicineId, String lotNumber);

    boolean existsByMedicine_MedicineIdAndLotNumberIgnoreCaseAndMedicineLotIdNot(
            Long medicineId,
            String lotNumber,
            Long medicineLotId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select ml
            from MedicineLot ml
            where ml.medicine.medicineId = :medicineId
              and ml.isActive = :isActive
            """)
    List<MedicineLot> findAllByMedicineIdAndIsActiveForUpdate(
            @Param("medicineId") Long medicineId,
            @Param("isActive") Integer isActive
    );

    @Query("""
            select ml
            from MedicineLot ml
            where ml.importDate >= :startDate
              and ml.importDate <= :endDate
            """)
    List<MedicineLot> findAllByImportDateBetween(
            @Param("startDate") java.time.LocalDate startDate,
            @Param("endDate") java.time.LocalDate endDate
    );
}