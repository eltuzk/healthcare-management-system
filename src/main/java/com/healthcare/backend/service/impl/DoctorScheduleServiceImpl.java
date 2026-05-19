package com.healthcare.backend.service.impl;

import com.healthcare.backend.dto.request.CreateDoctorScheduleRequest;
import com.healthcare.backend.dto.request.UpdateDoctorScheduleRequest;
import com.healthcare.backend.dto.response.DoctorScheduleImportResponse;
import com.healthcare.backend.dto.response.DoctorScheduleResponse;
import com.healthcare.backend.entity.Doctor;
import com.healthcare.backend.entity.DoctorSchedule;
import com.healthcare.backend.entity.Room;
import com.healthcare.backend.entity.enums.ShiftType;
import com.healthcare.backend.exception.BusinessException;
import com.healthcare.backend.exception.DuplicateResourceException;
import com.healthcare.backend.exception.ResourceNotFoundException;
import com.healthcare.backend.mapper.DoctorScheduleMapper;
import com.healthcare.backend.repository.DoctorRepository;
import com.healthcare.backend.repository.DoctorScheduleRepository;
import com.healthcare.backend.repository.RoomRepository;
import com.healthcare.backend.service.DoctorScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

@Service
@RequiredArgsConstructor
public class DoctorScheduleServiceImpl implements DoctorScheduleService {

    private static final String DOCTOR_SHIFT_CONSTRAINT = "UQ_SCHEDULE_DOCTOR_SHIFT";
    private static final String ROOM_SHIFT_CONSTRAINT = "UQ_SCHEDULE_ROOM_SHIFT";

    private final DoctorScheduleRepository doctorScheduleRepository;
    private final DoctorRepository doctorRepository;
    private final RoomRepository roomRepository;
    private final DoctorScheduleMapper doctorScheduleMapper;

    @Override
    // Transaction gom validate, gán bác sĩ/phòng và lưu lịch để unique constraint được kiểm tra nhất quán.
    @Transactional
    public DoctorScheduleResponse create(CreateDoctorScheduleRequest request) {
        validateScheduleDate(request.getScheduleDate());

        Doctor doctor = findDoctorOrThrow(request.getDoctorId());
        Room room = findRoomOrThrow(request.getRoomId());

        validateDuplicateDoctorShift(request.getDoctorId(), request.getScheduleDate(), request.getShift(), null);
        validateDuplicateRoomShift(request.getRoomId(), request.getScheduleDate(), request.getShift(), null);

        DoctorSchedule doctorSchedule = doctorScheduleMapper.toEntity(request);
        doctorSchedule.setDoctor(doctor);
        doctorSchedule.setRoom(room);

        return doctorScheduleMapper.toResponse(saveWithDuplicateHandling(doctorSchedule));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorScheduleResponse> getAll(LocalDate date, LocalDate startDate, LocalDate endDate, Long doctorId, Long roomId) {
        return doctorScheduleRepository.findAllByFilters(date, startDate, endDate, doctorId, roomId)
                .stream()
                .sorted(Comparator
                        .comparing(DoctorSchedule::getScheduleDate)
                        .thenComparing(doctorSchedule -> doctorSchedule.getShift().ordinal())
                        .thenComparing(doctorSchedule -> doctorSchedule.getDoctor().getDoctorId()))
                .map(doctorScheduleMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public DoctorScheduleImportResponse importSchedules(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("File tải lên trống");
        }

        List<DoctorSchedule> savedSchedules = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // Bỏ qua dòng tiêu đề (index 0)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String doctorName = getCellValueAsString(row.getCell(0));
                String roomCode = getCellValueAsString(row.getCell(1));

                if (doctorName.isEmpty() && roomCode.isEmpty()) {
                    continue; // Bỏ qua dòng trống
                }

                LocalDate scheduleDate = getCellValueAsDate(row.getCell(2));
                String shiftStr = getCellValueAsString(row.getCell(3));

                int currentRow = i + 1;

                // 1. Tìm bác sĩ
                List<Doctor> doctors = doctorRepository.findActiveByFullName(doctorName);
                if (doctors.isEmpty()) {
                    throw new BusinessException("Không tìm thấy bác sĩ đang hoạt động với tên: " + doctorName + " ở dòng " + currentRow);
                }
                Doctor doctor = doctors.get(0);

                // 2. Tìm phòng
                Room room = roomRepository.findByRoomCodeNormalized(roomCode)
                        .orElseThrow(() -> new BusinessException("Không tìm thấy phòng với mã: " + roomCode + " ở dòng " + currentRow));

                // 3. Validate ngày trực
                if (scheduleDate == null) {
                    throw new BusinessException("Ngày trực không hợp lệ ở dòng " + currentRow);
                }
                validateScheduleDate(scheduleDate);

                // 4. Validate ca trực
                ShiftType shift;
                try {
                    shift = ShiftType.valueOf(shiftStr.toUpperCase().trim());
                } catch (IllegalArgumentException e) {
                    throw new BusinessException("Ca trực không hợp lệ (MORNING, AFTERNOON, EVENING, NIGHT): " + shiftStr + " ở dòng " + currentRow);
                }

                // 5. Kiểm tra trùng lịch
                validateDuplicateDoctorShift(doctor.getDoctorId(), scheduleDate, shift, null);
                validateDuplicateRoomShift(room.getRoomId(), scheduleDate, shift, null);

                // 6. Tạo lịch
                DoctorSchedule schedule = new DoctorSchedule();
                schedule.setDoctor(doctor);
                schedule.setRoom(room);
                schedule.setScheduleDate(scheduleDate);
                schedule.setShift(shift);
                schedule.setMaxCapacity(0); // Cho mặc định = 0
                schedule.setCurrentBookingCount(0); 
                schedule.setLastQueueNumber(0);     
                schedule.setNote(null);             

                savedSchedules.add(saveWithDuplicateHandling(schedule));
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("Lỗi khi đọc file Excel: " + e.getMessage());
        }

        List<DoctorScheduleResponse> responses = savedSchedules.stream()
                .map(doctorScheduleMapper::toResponse)
                .toList();

        return new DoctorScheduleImportResponse(responses.size(), responses);
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue().trim();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((int) cell.getNumericCellValue());
        }
        return "";
    }

    private LocalDate getCellValueAsDate(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                String dateStr = cell.getStringCellValue().trim();
                if (dateStr.contains("/")) {
                    return LocalDate.parse(dateStr, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                }
                return LocalDate.parse(dateStr);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorScheduleResponse getById(Long doctorScheduleId) {
        return doctorScheduleMapper.toResponse(findScheduleOrThrow(doctorScheduleId));
    }

    @Override
    // Transaction dùng để cập nhật lịch và kiểm tra sức chứa không thấp hơn số slot đã được giữ.
    @Transactional
    public DoctorScheduleResponse update(Long doctorScheduleId, UpdateDoctorScheduleRequest request) {
        validateScheduleDate(request.getScheduleDate());

        DoctorSchedule existingSchedule = findScheduleOrThrow(doctorScheduleId);
        Doctor doctor = findDoctorOrThrow(request.getDoctorId());
        Room room = findRoomOrThrow(request.getRoomId());

        validateDuplicateDoctorShift(request.getDoctorId(), request.getScheduleDate(), request.getShift(), doctorScheduleId);
        validateDuplicateRoomShift(request.getRoomId(), request.getScheduleDate(), request.getShift(), doctorScheduleId);

        if (request.getMaxCapacity() < existingSchedule.getCurrentBookingCount()) {
            throw new BusinessException("Sức chứa tối đa không được nhỏ hơn số lượng đã đặt hiện tại");
        }

        doctorScheduleMapper.updateEntityFromRequest(request, existingSchedule);
        existingSchedule.setDoctor(doctor);
        existingSchedule.setRoom(room);

        return doctorScheduleMapper.toResponse(saveWithDuplicateHandling(existingSchedule));
    }

    @Override
    @Transactional
    public void delete(Long doctorScheduleId) {
        findScheduleOrThrow(doctorScheduleId);
        throw new BusinessException("Không hỗ trợ xóa lịch làm việc bác sĩ");
    }

    private DoctorSchedule findScheduleOrThrow(Long doctorScheduleId) {
        return doctorScheduleRepository.findById(doctorScheduleId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy lịch làm việc bác sĩ với id: " + doctorScheduleId
                ));
    }

    private Doctor findDoctorOrThrow(Long doctorId) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId)
                .or(() -> doctorRepository.findByAccount_AccountId(doctorId));
        
        if (doctorOpt.isEmpty()) {
            throw new ResourceNotFoundException("[SCHED] Không tìm thấy bác sĩ với id: " + doctorId);
        }
        
        Doctor doctor = doctorOpt.get();
        // Force activation of both doctor and account to be sure
        boolean changed = false;
        if (!doctor.isActive()) {
            doctor.setIsActive(1);
            changed = true;
        }
        if (doctor.getAccount() != null && !Integer.valueOf(1).equals(doctor.getAccount().getIsActive())) {
            doctor.getAccount().setIsActive(1);
            changed = true;
        }
        
        if (changed) {
            doctorRepository.save(doctor);
            System.out.println("[SCHED] Forced activation for doctor: " + doctor.getFullName());
        }
        
        return doctor;
    }

    private Room findRoomOrThrow(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng với id: " + roomId));
    }

    private void validateScheduleDate(LocalDate scheduleDate) {
        if (scheduleDate == null) {
            throw new BusinessException("Ngày làm việc không được để trống");
        }
        if (scheduleDate.isBefore(LocalDate.now())) {
            throw new BusinessException("Ngày làm việc không được ở trong quá khứ");
        }
    }

    private void validateDuplicateDoctorShift(Long doctorId, LocalDate scheduleDate, ShiftType shift, Long doctorScheduleId) {
        boolean duplicated = doctorScheduleId == null
                ? doctorScheduleRepository.existsByDoctor_DoctorIdAndScheduleDateAndShift(doctorId, scheduleDate, shift)
                : doctorScheduleRepository.existsByDoctor_DoctorIdAndScheduleDateAndShiftAndDoctorScheduleIdNot(
                        doctorId, scheduleDate, shift, doctorScheduleId
                );

        if (duplicated) {
            throw new DuplicateResourceException("Bác sĩ đã có lịch ở ca này trong ngày đã chọn");
        }
    }

    private void validateDuplicateRoomShift(Long roomId, LocalDate scheduleDate, ShiftType shift, Long doctorScheduleId) {
        boolean duplicated = doctorScheduleId == null
                ? doctorScheduleRepository.existsByRoom_RoomIdAndScheduleDateAndShift(roomId, scheduleDate, shift)
                : doctorScheduleRepository.existsByRoom_RoomIdAndScheduleDateAndShiftAndDoctorScheduleIdNot(
                        roomId, scheduleDate, shift, doctorScheduleId
                );

        if (duplicated) {
            throw new DuplicateResourceException("Phòng đã được phân công cho bác sĩ khác ở ca này trong ngày đã chọn");
        }
    }

    private DoctorSchedule saveWithDuplicateHandling(DoctorSchedule doctorSchedule) {
        try {
            return doctorScheduleRepository.saveAndFlush(doctorSchedule);
        } catch (DataIntegrityViolationException ex) {
            // Lớp bảo vệ cuối ở database: nếu hai request cùng vượt qua validate trùng ca,
            // unique constraint vẫn chặn và ta đổi lỗi DB thành lỗi nghiệp vụ dễ hiểu.
            String message = ex.getMostSpecificCause() != null
                    ? ex.getMostSpecificCause().getMessage()
                    : ex.getMessage();

            if (message != null) {
                String normalizedMessage = message.toUpperCase();

                if (normalizedMessage.contains(DOCTOR_SHIFT_CONSTRAINT)) {
                    throw new DuplicateResourceException("Bác sĩ đã có lịch ở ca này trong ngày đã chọn");
                }

                if (normalizedMessage.contains(ROOM_SHIFT_CONSTRAINT)) {
                    throw new DuplicateResourceException("Phòng đã được phân công cho bác sĩ khác ở ca này trong ngày đã chọn");
                }
            }

            throw ex;
        }
    }
    
}
