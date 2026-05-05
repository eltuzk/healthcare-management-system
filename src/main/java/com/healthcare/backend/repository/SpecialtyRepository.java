package com.healthcare.backend.repository;

import com.healthcare.backend.entity.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpecialtyRepository extends JpaRepository<Specialty, Long> {

    List<Specialty> findAllByOrderBySpecialtyNameAsc();

    boolean existsBySpecialtyCodeIgnoreCase(String specialtyCode);

    boolean existsBySpecialtyCodeIgnoreCaseAndSpecialtyIdNot(String specialtyCode, Long specialtyId);

    boolean existsBySpecialtyNameIgnoreCase(String specialtyName);

    boolean existsBySpecialtyNameIgnoreCaseAndSpecialtyIdNot(String specialtyName, Long specialtyId);
}
