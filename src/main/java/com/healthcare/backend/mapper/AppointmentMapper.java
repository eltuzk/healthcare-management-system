package com.healthcare.backend.mapper;

import com.healthcare.backend.dto.request.CreateAppointmentRequest;
import com.healthcare.backend.dto.request.CreateWalkInAppointmentRequest;
import com.healthcare.backend.dto.response.AppointmentResponse;
import com.healthcare.backend.entity.Appointment;
import com.healthcare.backend.entity.PaymentRecord;
import org.springframework.stereotype.Component;

@Component
public class AppointmentMapper {

    public Appointment toEntity(CreateAppointmentRequest request) {
        if (request == null) {
            return null;
        }

        Appointment appointment = new Appointment();
        appointment.setInitialSymptoms(request.getInitialSymptoms());
        appointment.setVisitReason(request.getVisitReason());
        return appointment;
    }

    public Appointment toEntity(CreateWalkInAppointmentRequest request) {
        if (request == null) {
            return null;
        }

        Appointment appointment = new Appointment();
        appointment.setInitialSymptoms(request.getInitialSymptoms());
        appointment.setVisitReason(request.getVisitReason());
        return appointment;
    }

    public AppointmentResponse toResponse(Appointment appointment) {
        if (appointment == null) {
            return null;
        }

        AppointmentResponse response = new AppointmentResponse();
        response.setAppointmentId(appointment.getAppointmentId());
        response.setAppointmentCode(appointment.getAppointmentCode());
        response.setQueueNum(appointment.getQueueNum());
        response.setStatus(appointment.getStatus());
        response.setInitialSymptoms(appointment.getInitialSymptoms());
        response.setVisitReason(appointment.getVisitReason());
        response.setCreatedAt(appointment.getCreatedAt());
        response.setUpdatedAt(appointment.getUpdatedAt());
        response.setPaidAt(appointment.getPaidAt());
        response.setPaymentExpiresAt(appointment.getPaymentExpiresAt());
        response.setSepayTransactionId(appointment.getSepayTransactionId());
        response.setPaymentReferenceCode(appointment.getPaymentReferenceCode());
        response.setPaymentContent(appointment.getPaymentContent());
        response.setCheckedInAt(appointment.getCheckedInAt());
        response.setCancelledAt(appointment.getCancelledAt());

        if (appointment.getPatient() != null) {
            response.setPatientId(appointment.getPatient().getPatientId());
            response.setPatientName(appointment.getPatient().getFullName());
        }

        if (appointment.getDoctorSchedule() != null) {
            response.setDoctorScheduleId(appointment.getDoctorSchedule().getDoctorScheduleId());
            response.setScheduleDate(appointment.getDoctorSchedule().getScheduleDate());
            response.setShift(appointment.getDoctorSchedule().getShift());

            if (appointment.getDoctorSchedule().getDoctor() != null) {
                response.setDoctorId(appointment.getDoctorSchedule().getDoctor().getDoctorId());
                response.setDoctorName(appointment.getDoctorSchedule().getDoctor().getFullName());
            }
        }

        if (appointment.getConsultationFee() != null) {
            response.setFeeId(appointment.getConsultationFee().getFeeId());
        }
        response.setFeeName(appointment.getFeeNameSnapshot());
        response.setFeePrice(appointment.getFeePriceSnapshot());

        PaymentRecord paymentRecord = appointment.getPaymentRecord();
        if (paymentRecord != null) {
            response.setExpectedPaymentAmount(paymentRecord.getTotalPrice());
            response.setReceivedPaymentAmount(paymentRecord.getReceivedAmount());
            response.setPaymentStatus(paymentRecord.getPaymentStatus());
        }

        return response;
    }
}
