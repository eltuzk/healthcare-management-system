package com.healthcare.backend.dto.response;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RoomResponse {

    private Long roomId;
    private String roomCode;
    private String position;
    private String note;
    private Long roomTypeId;
    private String roomTypeName;
    private Long branchId;
    private String branchName;
}

