package com.healthcare.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "admission_record")
@Getter
@Setter
@NoArgsConstructor
public class AdmissionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admission_record_id")
    private Long admissionRecordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admission_id", nullable = false)
    private AdmissionRequest admissionRequest;

    @Column(name = "blood_pressure", nullable = false, length = 20)
    private String bloodPressure;

    @Column(name = "heart_rate", nullable = false)
    private Integer heartRate;

    @Column(name = "temperature", nullable = false)
    private Double temperature;

    @Column(name = "record_date", nullable = false)
    private LocalDateTime recordDate;

    @PrePersist
    public void prePersist() {
        if (recordDate == null) {
            recordDate = LocalDateTime.now();
        }
    }
}