package com.example.backend.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductRequest {
    private Long categoryId;
    private String productName;
    private String brand;
    private String description;
    private Double price;
    private Integer stockQuantity;
    private List<String> imageUrls;
}
