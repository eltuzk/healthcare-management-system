package com.healthcare.backend.entity.enums;

public enum ShiftType {
    MORNING("07:00", "11:00"),
    AFTERNOON("13:00", "17:00");

    private final String startTime;
    private final String endTime;

    ShiftType(String startTime, String endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }
}
