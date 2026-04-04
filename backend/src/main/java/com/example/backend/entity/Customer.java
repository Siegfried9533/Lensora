package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "customers")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true) // Số điện thoại là định danh để kiểm tra khách cũ/mới
    private String phone;

    @Column(unique = true)
    private String email;

    private String identityCard; // Số CCCD để xác minh khi thuê

    private Double trustScore = 100.0; // Mặc định 100 điểm uy tín

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Constructor nhanh để dùng trong Service
    public Customer(String phone, String fullName) {
        this.phone = phone;
        this.fullName = fullName;
        this.trustScore = 100.0;
        this.createdAt = LocalDateTime.now();
    }
}