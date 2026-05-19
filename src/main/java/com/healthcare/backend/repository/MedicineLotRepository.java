package com.healthcare.backend.repository;

import com.healthcare.backend.entity.MedicineLot;
import org.springframework.data.jpa.repository.JpaRepository;
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
}