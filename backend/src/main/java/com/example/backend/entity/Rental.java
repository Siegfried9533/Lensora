package com.example.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Data;
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
    private Long rentalId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "asset_id")
    private Asset asset;

    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate returnDate;
    private Double depositFee;
    private Double totalRentFee;
    private Double penaltyFee;
    private String status; // 'PENDING' | 'ACTIVE' | 'COMPLETED' | 'CANCELLED';
}
