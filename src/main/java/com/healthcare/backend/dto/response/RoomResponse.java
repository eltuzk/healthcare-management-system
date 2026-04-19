package com.healthcare.backend.dto.response;

public class RoomResponse {

    private Long roomId;
    private String roomCode;
    private String position;
    private String note;
    private Long roomTypeId;
    private String roomTypeName;
    private Integer branchId;
    private String branchName;

    public RoomResponse() {
    }

    public RoomResponse(Long roomId, String roomCode, String position, String note,
                        Long roomTypeId, String roomTypeName,
                        Integer branchId, String branchName) {
        this.roomId = roomId;
        this.roomCode = roomCode;
        this.position = position;
        this.note = note;
        this.roomTypeId = roomTypeId;
        this.roomTypeName = roomTypeName;
        this.branchId = branchId;
        this.branchName = branchName;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
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

    public Integer getBranchId() {
        return branchId;
    }

    public void setBranchId(Integer branchId) {
        this.branchId = branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }
}