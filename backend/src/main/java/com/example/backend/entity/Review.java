package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "Reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    private Long userId;
    private Long entityId; // productId or assetId
    private Integer rating;
    private String comment;
    private String type; // 'PRODUCT' | 'ASSET'
}
