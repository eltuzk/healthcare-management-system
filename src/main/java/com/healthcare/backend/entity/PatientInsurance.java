package com.healthcare.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "PATIENT_INSURANCE")
@Getter
@Setter
@NoArgsConstructor
public class PatientInsurance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "patient_insurance_id")
    private Long patientInsuranceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "insurance_num", unique = true, nullable = false, length = 100)
    private String insuranceNum;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "coverage_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal coveragePercent;
}
