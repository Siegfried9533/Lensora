package com.example.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Data;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;


@Entity
@Table(name = "Rentals")
@Data
public class Rental {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Thông tin về camera được thuê
    @ManyToOne
    @JoinColumn(name = "camera_id")
    private Camera camera;

    // Thong tin về khách hàng thuê camera
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    // Thông tin về ngày thuê và trả camera
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate returnDate;

    // Giá thuê
    private Double totalRentAmount; // Tổng số tiền thuê
    private Double depositAmount; // Số tiền đặt cọc

    // Trạng thái của đơn thuê (PENDING, ACTIVE, COMPLETED, CANCELLED)
    @Enumerated(EnumType.STRING)
    private RentalStatus status; // Trạng thái của đơn thuê
}
