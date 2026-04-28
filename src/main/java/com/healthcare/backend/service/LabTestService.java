package com.healthcare.backend.service;

import com.healthcare.backend.dto.request.LabTestRequest;
import com.healthcare.backend.dto.response.LabTestResponse;

import java.util.List;

public interface LabTestService {

    List<LabTestResponse> getAllLabTests();

    LabTestResponse getLabTestById(Long id);

    LabTestResponse createLabTest(LabTestRequest request);

    LabTestResponse updateLabTest(Long id, LabTestRequest request);

    LabTestResponse deactivateLabTest(Long id);
}