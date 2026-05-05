package com.healthcare.backend.scheduler;

import com.healthcare.backend.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppointmentPaymentReservationScheduler {

    private final AppointmentService appointmentService;

    @Scheduled(fixedDelayString = "${appointments.payment-expiry-scan-delay-ms:60000}")
    public void expirePendingPaymentReservations() {
        appointmentService.expirePendingPaymentReservations();
    }
}
