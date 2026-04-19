package com.healthcare.backend.dto.response;

public class RoomTypeResponseDTO {

    private Long roomTypeId;
    private String roomTypeName;

    public RoomTypeResponseDTO() {
    }

    public RoomTypeResponseDTO(Long roomTypeId, String roomTypeName) {
        this.roomTypeId = roomTypeId;
        this.roomTypeName = roomTypeName;
    }

    public Long getRoomTypeId() {
        return roomTypeId;
    }

    public void setRoomTypeId(Long roomTypeId) {
        this.roomTypeId = roomTypeId;
    }

    public String getRoomTypeName() {
        return roomTypeName;
    }

    public void setRoomTypeName(String roomTypeName) {
        this.roomTypeName = roomTypeName;
    }
}