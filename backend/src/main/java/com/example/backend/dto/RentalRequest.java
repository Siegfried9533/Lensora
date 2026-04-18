package com.example.backend.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;

@Data
public class RentalRequest {
    // đưa vào thông tin đặt thiết bị
    private Long assetId; // id thiết bị thuê

    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date must be in the future or present")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDate endDate;

    @NotNull(message = "return Date is required")
    @Future(message = "return Date must be in the future")
    private LocalDate returnDate;
}
