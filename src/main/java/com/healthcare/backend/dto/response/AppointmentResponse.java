package com.healthcare.backend.dto.response;

import com.healthcare.backend.entity.enums.AppointmentStatus;
import com.healthcare.backend.entity.enums.PaymentStatus;
import com.healthcare.backend.entity.enums.ShiftType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {

    private Long appointmentId;
    private Long patientId;
    private String patientName;
    private Long doctorScheduleId;
    private Long doctorId;
    private String doctorName;
    private Long feeId;
    private String feeName;
    private BigDecimal feePrice;
    private LocalDate scheduleDate;
    private ShiftType shift;
    private String appointmentCode;
    private Integer queueNum;
    private AppointmentStatus status;
    private String initialSymptoms;
    private String visitReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime paidAt;
    private LocalDateTime paymentExpiresAt;
    private BigDecimal expectedPaymentAmount;
    private BigDecimal receivedPaymentAmount;
    private PaymentStatus paymentStatus;
    private Long sepayTransactionId;
    private String paymentReferenceCode;
    private String paymentContent;
    private LocalDateTime checkedInAt;
    private LocalDateTime cancelledAt;
}
