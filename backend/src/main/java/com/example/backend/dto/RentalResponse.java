package com.example.backend.dto;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RentalResponse {
    // đưa vào thông tin vào UI
    private Long rentalId;

    // Thông tin người thuê
    private String userName;
    private String email;

    // Thông tin tài sản (để hiện lên UI đơn hàng)
    private Long assetId;
    private String modelName;
    private String brand;
    private String primaryImageUrl; // Ảnh đại diện của máy

    // Chi tiết đơn thuê
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate returnDate;

    private Double depositFee;
    private Double totalRentFee;
    private Double penaltyFee;
    private String status;
}
