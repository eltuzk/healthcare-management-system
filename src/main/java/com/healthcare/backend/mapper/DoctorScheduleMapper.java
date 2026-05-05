package com.healthcare.backend.mapper;

import com.healthcare.backend.dto.request.CreateDoctorScheduleRequest;
import com.healthcare.backend.dto.request.UpdateDoctorScheduleRequest;
import com.healthcare.backend.dto.response.DoctorScheduleResponse;
import com.healthcare.backend.entity.DoctorSchedule;
import org.springframework.stereotype.Component;

@Component
public class DoctorScheduleMapper {

    public DoctorSchedule toEntity(CreateDoctorScheduleRequest request) {
        if (request == null) {
            return null;
        }

        DoctorSchedule doctorSchedule = new DoctorSchedule();
        doctorSchedule.setScheduleDate(request.getScheduleDate());
        doctorSchedule.setShift(request.getShift());
        doctorSchedule.setMaxCapacity(request.getMaxCapacity());
        doctorSchedule.setCurrentBookingCount(0);
        doctorSchedule.setLastQueueNumber(0);
        doctorSchedule.setNote(request.getNote());
        return doctorSchedule;
    }

    public void updateEntityFromRequest(UpdateDoctorScheduleRequest request, DoctorSchedule doctorSchedule) {
        if (request == null || doctorSchedule == null) {
            return;
        }

        doctorSchedule.setScheduleDate(request.getScheduleDate());
        doctorSchedule.setShift(request.getShift());
        doctorSchedule.setMaxCapacity(request.getMaxCapacity());
        doctorSchedule.setNote(request.getNote());
    }

    public DoctorScheduleResponse toResponse(DoctorSchedule doctorSchedule) {
        if (doctorSchedule == null) {
            return null;
        }

        DoctorScheduleResponse response = new DoctorScheduleResponse();
        response.setDoctorScheduleId(doctorSchedule.getDoctorScheduleId());
        response.setScheduleDate(doctorSchedule.getScheduleDate());
        response.setShift(doctorSchedule.getShift());
        response.setMaxCapacity(doctorSchedule.getMaxCapacity());
        response.setCurrentBookingCount(doctorSchedule.getCurrentBookingCount());
        response.setLastQueueNumber(doctorSchedule.getLastQueueNumber());
        response.setNote(doctorSchedule.getNote());
        response.setCreatedAt(doctorSchedule.getCreatedAt());
        response.setUpdatedAt(doctorSchedule.getUpdatedAt());

        if (doctorSchedule.getShift() != null) {
            response.setShiftStartTime(doctorSchedule.getShift().getStartTime());
            response.setShiftEndTime(doctorSchedule.getShift().getEndTime());
        }

        if (doctorSchedule.getDoctor() != null) {
            response.setDoctorId(doctorSchedule.getDoctor().getDoctorId());
            response.setDoctorName(doctorSchedule.getDoctor().getFullName());
        }

        if (doctorSchedule.getRoom() != null) {
            response.setRoomId(doctorSchedule.getRoom().getRoomId());
            response.setRoomCode(doctorSchedule.getRoom().getRoomCode());

            if (doctorSchedule.getRoom().getBranch() != null) {
                response.setBranchId(doctorSchedule.getRoom().getBranch().getBranchId());
                response.setBranchName(doctorSchedule.getRoom().getBranch().getBranchName());
            }
        }

        return response;
    }
}
