package com.healthcare.backend.service;

import com.healthcare.backend.dto.request.MedicineLotRequest;
import com.healthcare.backend.dto.response.MedicineLotResponse;

import java.util.List;

public interface MedicineLotService {

    List<MedicineLotResponse> getAllMedicineLots(Long medicineId);

    MedicineLotResponse getMedicineLotById(Long id);

    MedicineLotResponse createMedicineLot(MedicineLotRequest request);

    MedicineLotResponse updateMedicineLot(Long id, MedicineLotRequest request);

    MedicineLotResponse deactivateMedicineLot(Long id);
}
