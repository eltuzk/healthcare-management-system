package com.healthcare.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabTestResultResponse {
    private Long labTestResultId;
    private Long labTestRequestId;
    private String resultData;
    private LocalDateTime resultDate;
}
