package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "Images")
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;

    private Long entityId; // productId or assetId
    private String url;
    private Boolean isPrimary;
    private String type; // 'PRODUCT' | 'ASSET'
}
