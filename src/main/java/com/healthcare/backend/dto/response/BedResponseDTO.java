package com.healthcare.backend.dto.response;

import java.math.BigDecimal;

public class BedResponseDTO {

    private Long bedId;
    private BigDecimal price;
    private String status;
    private Long roomId;

    public BedResponseDTO() {
    }

    public BedResponseDTO(Long bedId, BigDecimal price, String status, Long roomId) {
        this.bedId = bedId;
        this.price = price;
        this.status = status;
        this.roomId = roomId;
    }

    public Long getBedId() {
        return bedId;
    }

    public void setBedId(Long bedId) {
        this.bedId = bedId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }
}