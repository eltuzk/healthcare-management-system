package com.healthcare.backend.dto.request;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Getter
@Setter
public class RoomRequest {

    @NotBlank(message = "Mã phòng không được để trống")
    @Size(max = 50, message = "Mã phòng không được vượt quá 50 ký tự")
    private String roomCode;

    @Size(max = 200, message = "Vị trí không được vượt quá 200 ký tự")
    private String position;

    @Size(max = 500, message = "Ghi chú không được vượt quá 500 ký tự")
    private String note;

    @NotNull(message = "Loại phòng không được để trống")
    private Long roomTypeId;

    @NotNull(message = "Chi nhánh không được để trống")
    private Long branchId;
}
