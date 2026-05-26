package com.healthcare.backend.service.impl;

import com.healthcare.backend.dto.request.PrescriptionDetailRequest;
import com.healthcare.backend.dto.request.PrescriptionRequest;
import com.healthcare.backend.dto.response.PrescriptionResponse;
import com.healthcare.backend.entity.MedicalRecord;
import com.healthcare.backend.entity.Medicine;
import com.healthcare.backend.entity.Prescription;
import com.healthcare.backend.entity.PrescriptionDetail;
import com.healthcare.backend.entity.MedicineLot;
import com.healthcare.backend.entity.enums.MedicalRecordStatus;
import com.healthcare.backend.exception.BusinessException;
import com.healthcare.backend.exception.ResourceNotFoundException;
import com.healthcare.backend.mapper.PrescriptionDetailMapper;
import com.healthcare.backend.mapper.PrescriptionMapper;
import com.healthcare.backend.repository.MedicalRecordRepository;
import com.healthcare.backend.repository.MedicineLotRepository;
import com.healthcare.backend.repository.MedicineRepository;
import com.healthcare.backend.repository.PrescriptionRepository;
import com.healthcare.backend.entity.PaymentRecord;
import com.healthcare.backend.entity.enums.PaymentStatus;
import com.healthcare.backend.repository.PaymentRecordRepository;
import com.healthcare.backend.service.PrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PrescriptionServiceImpl implements PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final MedicineRepository medicineRepository;
    private final MedicineLotRepository medicineLotRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final PrescriptionMapper prescriptionMapper;
    private final PrescriptionDetailMapper prescriptionDetailMapper;

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getAllPrescriptions() {
        return prescriptionRepository.findAllByIsActiveOrderByCreatedAtDesc(1)
                .stream()
                .map(prescriptionMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PrescriptionResponse getPrescriptionById(Long id) {
        Prescription prescription = findActivePrescriptionById(id);
        return prescriptionMapper.toResponse(prescription);
    }

    @Override
    @Transactional(readOnly = true)
    public PrescriptionResponse getPrescriptionByMedicalRecordId(Long medicalRecordId) {
        Prescription prescription = prescriptionRepository
                .findByMedicalRecord_MedicalRecordIdAndIsActive(medicalRecordId, 1)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Prescription not found with medical record id: " + medicalRecordId
                ));

        return prescriptionMapper.toResponse(prescription);
    }

    @Override
    @Transactional
    public PrescriptionResponse createPrescription(PrescriptionRequest request) {
        MedicalRecord medicalRecord = findMedicalRecordForUpdateOrThrow(request.getMedicalRecordId());

        validateMedicalRecordCompleted(medicalRecord);
        validatePrescriptionNotExists(request.getMedicalRecordId());
        validatePrescriptionDetails(request.getDetails());

        // Check if a deactivated prescription exists for this medical record (unique constraint on med_record_id)
        java.util.Optional<Prescription> deactivatedOpt = prescriptionRepository
                .findByMedicalRecord_MedicalRecordIdAndIsActive(request.getMedicalRecordId(), 0);

        Prescription prescription;
        if (deactivatedOpt.isPresent()) {
            // Reactivate the existing deactivated prescription instead of creating a new one
            prescription = deactivatedOpt.get();
            prescription.setIsActive(1);
            prescription.setNote(request.getNote() != null ? request.getNote().trim() : null);
            prescription.getPrescriptionDetails().clear();
            addPrescriptionDetails(prescription, request.getDetails());
        } else {
            prescription = prescriptionMapper.toEntity(request, medicalRecord);
            addPrescriptionDetails(prescription, request.getDetails());
        }

        Prescription savedPrescription = prescriptionRepository.save(prescription);
        createOrUpdatePrescriptionPayment(savedPrescription);
        return prescriptionMapper.toResponse(savedPrescription);
    }

    @Override
    @Transactional
    public PrescriptionResponse updatePrescription(Long id, PrescriptionRequest request) {
        Prescription prescription = findActivePrescriptionById(id);
        MedicalRecord medicalRecord = findMedicalRecordForUpdateOrThrow(request.getMedicalRecordId());

        validateMedicalRecordCompleted(medicalRecord);
        validatePrescriptionDetails(request.getDetails());

        if (!prescription.getMedicalRecord().getMedicalRecordId().equals(request.getMedicalRecordId())
                && prescriptionRepository.existsByMedicalRecord_MedicalRecordIdAndIsActive(request.getMedicalRecordId(), 1)) {
            throw new BusinessException("Prescription already exists for this medical record");
        }

        prescription.setMedicalRecord(medicalRecord);
        prescriptionMapper.updateEntityFromRequest(request, prescription);

        prescription.getPrescriptionDetails().clear();
        addPrescriptionDetails(prescription, request.getDetails());

        Prescription updatedPrescription = prescriptionRepository.save(prescription);
        createOrUpdatePrescriptionPayment(updatedPrescription);
        return prescriptionMapper.toResponse(updatedPrescription);
    }

    @Override
    @Transactional
    public PrescriptionResponse deactivatePrescription(Long id) {
        Prescription prescription = findActivePrescriptionById(id);

        prescription.setIsActive(0);
        Prescription deactivatedPrescription = prescriptionRepository.save(prescription);

        return prescriptionMapper.toResponse(deactivatedPrescription);
    }

    @Override
    @Transactional
    public PrescriptionResponse dispensePrescription(Long id) {
        Prescription prescription = findActivePrescriptionById(id);

        PaymentRecord paymentRecord = paymentRecordRepository.findByPrescription_PrescriptionId(id)
                .orElseThrow(() -> new BusinessException("Hóa đơn thanh toán cho đơn thuốc này không tồn tại."));
        if (paymentRecord.getPaymentStatus() != PaymentStatus.PAID) {
            throw new BusinessException("Đơn thuốc chưa được thanh toán. Vui lòng thanh toán trước khi cấp phát.");
        }

        for (PrescriptionDetail detail : prescription.getPrescriptionDetails()) {
            Medicine medicine = detail.getMedicine();
            int requiredQty = detail.getQuantity();

            // Concurrency Control: Acquire Pessimistic Write Lock on all active lots of this medicine
            List<MedicineLot> lots = medicineLotRepository.findAllByMedicineIdAndIsActiveForUpdate(medicine.getMedicineId(), 1);

            // Sort lots by expiryDate ASC (FIFO - First Expired, First Out) and exclude expired/empty lots
            LocalDate today = LocalDate.now();
            List<MedicineLot> eligibleLots = lots.stream()
                    .filter(lot -> lot.getQuantity() > 0)
                    .filter(lot -> lot.getExpiryDate() == null || !lot.getExpiryDate().isBefore(today))
                    .sorted((l1, l2) -> {
                        if (l1.getExpiryDate() == null && l2.getExpiryDate() == null) return 0;
                        if (l1.getExpiryDate() == null) return 1;
                        if (l2.getExpiryDate() == null) return -1;
                        return l1.getExpiryDate().compareTo(l2.getExpiryDate());
                    })
                    .toList();

            int totalStock = eligibleLots.stream().mapToInt(MedicineLot::getQuantity).sum();
            if (requiredQty > totalStock) {
                throw new BusinessException("Dược phẩm '" + medicine.getMedicineName()
                        + "' không đủ tồn kho khả dụng để cấp phát. Yêu cầu: "
                        + requiredQty + ", Có sẵn: " + totalStock);
            }

            // FIFO stock subtraction
            int remainingToDeduct = requiredQty;
            for (MedicineLot lot : eligibleLots) {
                if (remainingToDeduct <= 0) break;

                int currentLotQty = lot.getQuantity();
                if (currentLotQty >= remainingToDeduct) {
                    lot.setQuantity(currentLotQty - remainingToDeduct);
                    remainingToDeduct = 0;
                } else {
                    lot.setQuantity(0);
                    remainingToDeduct -= currentLotQty;
                }
                medicineLotRepository.save(lot);
            }
        }

        // Mark prescription as dispensed (set isActive to 0)
        prescription.setIsActive(0);
        Prescription savedPrescription = prescriptionRepository.save(prescription);

        return prescriptionMapper.toResponse(savedPrescription);
    }

    private Prescription findActivePrescriptionById(Long id) {
        return prescriptionRepository.findByPrescriptionIdAndIsActive(id, 1)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found with id: " + id));
    }

    private MedicalRecord findMedicalRecordForUpdateOrThrow(Long medicalRecordId) {
        return medicalRecordRepository.findByIdForUpdate(medicalRecordId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Medical record not found with id: " + medicalRecordId
                ));
    }

    private Medicine findActiveMedicineById(Long medicineId) {
        return medicineRepository.findByMedicineIdAndIsActive(medicineId, 1)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with id: " + medicineId));
    }

    private void validateMedicalRecordCompleted(MedicalRecord medicalRecord) {
        if (medicalRecord.getStatus() != MedicalRecordStatus.COMPLETED) {
            throw new BusinessException("Only completed medical records can have prescriptions");
        }
    }

    private void validatePrescriptionNotExists(Long medicalRecordId) {
        if (prescriptionRepository.existsByMedicalRecord_MedicalRecordIdAndIsActive(medicalRecordId, 1)) {
            throw new BusinessException("Prescription already exists for this medical record");
        }
    }

    private void validatePrescriptionDetails(List<PrescriptionDetailRequest> details) {
        if (details == null || details.isEmpty()) {
            throw new BusinessException("Prescription details must not be empty");
        }

        for (PrescriptionDetailRequest detail : details) {
            if (detail.getQuantity() == null || detail.getQuantity() < 1) {
                throw new BusinessException("Prescription detail quantity must be greater than or equal to 1");
            }

            Long medicineId = detail.getMedicineId();
            Medicine medicine = findActiveMedicineById(medicineId);

            // Concurrency Control: Acquire Pessimistic Write Lock on all lots of this medicine
            List<MedicineLot> lots = medicineLotRepository.findAllByMedicineIdAndIsActiveForUpdate(medicineId, 1);
            
            // Sum available non-expired stock
            int totalStock = 0;
            LocalDate today = LocalDate.now();
            if (lots != null) {
                for (MedicineLot lot : lots) {
                    if (lot.getExpiryDate() != null && !lot.getExpiryDate().isBefore(today)) {
                        totalStock += lot.getQuantity();
                    }
                }
            }

            if (detail.getQuantity() > totalStock) {
                throw new BusinessException("Dược phẩm '" + medicine.getMedicineName() + "' không đủ tồn kho khả dụng. Tồn kho thực tế: " + totalStock + ", Yêu cầu kê đơn: " + detail.getQuantity());
            }
        }
    }

    private void addPrescriptionDetails(Prescription prescription, List<PrescriptionDetailRequest> detailRequests) {
        for (PrescriptionDetailRequest detailRequest : detailRequests) {
            Medicine medicine = findActiveMedicineById(detailRequest.getMedicineId());
            PrescriptionDetail detail = prescriptionDetailMapper.toEntity(detailRequest, prescription, medicine);
            prescription.getPrescriptionDetails().add(detail);
        }
    }

    private void createOrUpdatePrescriptionPayment(Prescription prescription) {
        java.math.BigDecimal totalPrice = java.math.BigDecimal.ZERO;
        for (PrescriptionDetail detail : prescription.getPrescriptionDetails()) {
            java.math.BigDecimal sellingPrice = detail.getMedicine().getSellingPrice();
            if (sellingPrice != null) {
                totalPrice = totalPrice.add(sellingPrice.multiply(java.math.BigDecimal.valueOf(detail.getQuantity())));
            }
        }

        PaymentRecord paymentRecord = paymentRecordRepository.findByPrescription_PrescriptionId(prescription.getPrescriptionId())
                .orElse(null);

        if (paymentRecord == null) {
            paymentRecord = new PaymentRecord();
            paymentRecord.setPrescription(prescription);
            paymentRecord.setRequestCode("PR-" + prescription.getPrescriptionId());
            paymentRecord.setTotalPrice(totalPrice);
            paymentRecord.setReceivedAmount(java.math.BigDecimal.ZERO);
            paymentRecord.setPaymentStatus(PaymentStatus.UNPAID);
        } else {
            if (paymentRecord.getPaymentStatus() != PaymentStatus.PAID) {
                paymentRecord.setTotalPrice(totalPrice);
            }
        }

        paymentRecordRepository.save(paymentRecord);
    }
}