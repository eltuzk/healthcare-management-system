package com.healthcare.backend.mapper;

import com.healthcare.backend.dto.request.AdmissionRecordRequest;
import com.healthcare.backend.dto.response.AdmissionRecordResponse;
import com.healthcare.backend.entity.AdmissionRecord;
import org.springframework.stereotype.Component;

@Component
public class AdmissionRecordMapper {


    public AdmissionRecord toEntity(AdmissionRecordRequest request) {
        AdmissionRecord entity = new AdmissionRecord();
        entity.setBloodPressure(request.getBloodPressure());
        entity.setHeartRate(request.getHeartRate());
        entity.setTemperature(request.getTemperature());

        return entity;
    }

    public AdmissionRecordResponse toResponse(AdmissionRecord entity) {
        AdmissionRecordResponse response = new AdmissionRecordResponse();
        response.setAdmissionRecordId(entity.getAdmissionRecordId());
        response.setBloodPressure(entity.getBloodPressure());
        response.setHeartRate(entity.getHeartRate());
        response.setTemperature(entity.getTemperature());
        response.setRecordDate(entity.getRecordDate());

        if (entity.getAdmissionRequest() != null) {
            response.setAdmissionId(entity.getAdmissionRequest().getAdmissionId());
        }

        return response;
    }

    public void updateEntityFromRequest(AdmissionRecordRequest request, AdmissionRecord entity) {
        if (request.getBloodPressure() != null) entity.setBloodPressure(request.getBloodPressure());
        if (request.getHeartRate()     != null) entity.setHeartRate(request.getHeartRate());
        if (request.getTemperature()   != null) entity.setTemperature(request.getTemperature());
    }
}