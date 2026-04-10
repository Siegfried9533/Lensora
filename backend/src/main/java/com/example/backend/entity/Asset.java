package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "Assets")
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long assetId;

    @Column(nullable = false)
    private Long categoryId;

    @Column(nullable = false)
    private String modelName;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private Double dailyRate;

    @Column(nullable = false) // 'AVAILABLE' | 'RENTED' | 'MAINTENANCE';
    private String status;

    @Column(unique = true)
    private String serialNumber;
}
