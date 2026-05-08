package com.healthcare.backend.service;

import com.healthcare.backend.entity.MedicalRecord;

public interface MedicalRecordBillingService {

    void initializePaymentRecord(MedicalRecord medicalRecord);

    void syncBilling(Long medicalRecordId);
}
