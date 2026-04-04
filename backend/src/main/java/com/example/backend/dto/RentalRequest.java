package com.example.backend.dto;

import lombok.Data;

@Data
public class RentalRequest {
    private Long cameraId;
    private Long customerId;
    private Integer rentalDays; // Thời gian thuê (tính bằng ngày)
}
