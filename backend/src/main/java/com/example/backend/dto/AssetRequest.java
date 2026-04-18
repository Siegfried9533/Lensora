package com.example.backend.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AssetRequest {
    // ID của Asset tự sinh, không cần cung cấp trong request
    private Long categoryId; // ID của Category, sẽ được sử dụng để tìm kiếm Category trong DB
    private String modelName;
    private String brand;
    private String description;
    private Double price;
    private Double dailyRate;
    private Double depositValue;
    private String serialNumber;
    private List<String> imageUrls;
}
