package com.healthcare.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "lab_test_result")
@Getter
@Setter
@NoArgsConstructor
public class    LabTestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lab_test_result_id")
    private Long labTestResultId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_test_request_id", nullable = false, unique = true)
    private LabTestRequest labTestRequest;

    @Lob
    @Column(name = "result_data")
    private String resultData;

    @Column(name = "result_date")
    private LocalDateTime resultDate;

    @PrePersist
    public void prePersist() {
        if (this.resultDate == null) {
            this.resultDate = LocalDateTime.now();
        }
    }
}
