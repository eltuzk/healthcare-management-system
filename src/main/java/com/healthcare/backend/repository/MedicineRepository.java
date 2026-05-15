package com.healthcare.backend.repository;

import com.healthcare.backend.entity.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    List<Medicine> findAllByIsActiveOrderByMedicineNameAsc(Integer isActive);

    Optional<Medicine> findByMedicineIdAndIsActive(Long medicineId, Integer isActive);

    boolean existsByMedicineNameIgnoreCase(String medicineName);

    boolean existsByMedicineNameIgnoreCaseAndMedicineIdNot(String medicineName, Long medicineId);
}