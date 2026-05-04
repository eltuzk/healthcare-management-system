package com.healthcare.backend.service;

import com.healthcare.backend.dto.request.CreateAppointmentRequest;
import com.healthcare.backend.dto.request.CreateWalkInAppointmentRequest;
import com.healthcare.backend.dto.request.SepayWebhookRequest;
import com.healthcare.backend.dto.response.AppointmentResponse;
import com.healthcare.backend.entity.enums.AppointmentStatus;

import java.util.List;

public interface AppointmentService {

    AppointmentResponse create(CreateAppointmentRequest request);

    AppointmentResponse createWalkInPaidAppointment(CreateWalkInAppointmentRequest request);

    AppointmentResponse getById(Long appointmentId);

    List<AppointmentResponse> getAll(Long patientId, Long doctorScheduleId, AppointmentStatus status);

    AppointmentResponse checkIn(Long appointmentId);

    AppointmentResponse start(Long appointmentId);

    AppointmentResponse cancel(Long appointmentId);

    AppointmentResponse confirmPaymentFromSepayWebhook(SepayWebhookRequest request, String secretKeyHeader);

    void expirePendingPaymentReservations();
}
