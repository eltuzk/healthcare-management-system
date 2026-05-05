package com.healthcare.backend.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class SpecialtyResponse {

    private Long specialtyId;
    private String specialtyCode;
    private String specialtyName;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
