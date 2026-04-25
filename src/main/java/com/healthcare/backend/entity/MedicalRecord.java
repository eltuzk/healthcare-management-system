package com.healthcare.backend.entity;

import com.healthcare.backend.entity.enums.MedicalRecordConclusionType;
import com.healthcare.backend.entity.enums.MedicalRecordStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "medical_record")
@Getter
@Setter
@NoArgsConstructor
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "med_record_id")
    private Long medicalRecordId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "initial_diagnosis", nullable = false, length = 1000)
    private String initialDiagnosis;

    @Lob
    @Column(name = "clinical_conclusion")
    private String clinicalConclusion;

    @Enumerated(EnumType.STRING)
    @Column(name = "conclusion_type", length = 30)
    private MedicalRecordConclusionType conclusionType;

    @Lob
    @Column(name = "clinical_notes")
    private String clinicalNotes;

    @Lob
    @Column(name = "treatment_plan")
    private String treatmentPlan;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MedicalRecordStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "locked_at")
    private LocalDateTime lockedAt;

    // Optimistic locking: tránh bác sĩ ghi đè nội dung hồ sơ khi nhiều tab/request cùng chỉnh sửa.
    @Version
    @Column(name = "version_number", nullable = false)
    private Long versionNumber;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = MedicalRecordStatus.DRAFT;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
