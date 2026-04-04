package com.example.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

@Entity
@Data
@Table(name = "cameras")
public class Camera {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String brand;
    private String model;
    private Double dailyRate; // Giá thuê hàng ngày
    private Double depositValue; // Giá trị đặt cọc

    @Enumerated(EnumType.STRING)
    private CameraStatus status; // Trạng thái của camera (AVAILABLE, RENTING, MAINTENANCE)
    private String imageUrl; // URL hình ảnh của camera

}