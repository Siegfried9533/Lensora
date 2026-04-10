package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "Orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    private Long userId;
    private LocalDateTime orderDate;
    private Double totalAmount;
    private String shippingAddress;
    private String status; // 'PENDING' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED';
}
