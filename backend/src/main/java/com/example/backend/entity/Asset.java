package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Assets")
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long assetId;

    @ManyToOne // Kết nối trực tiếp với bảng Category
    @JoinColumn(name = "categoryId")
    private Category category;

    private String modelName;
    private String brand;
    private String description;

    private Double dailyRate; // Giá thuê theo ngày
    private Double depositValue; // tiến cọc

    private String status; // 'AVAILABLE', 'RENTED', 'MAINTENANCE'
    private String serialNumber;
}
