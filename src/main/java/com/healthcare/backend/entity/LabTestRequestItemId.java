package com.healthcare.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class LabTestRequestItemId implements Serializable {

    @Column(name = "lab_test_request_id")
    private Long labTestRequestId;

    @Column(name = "lab_test_id")
    private Long labTestId;
}
