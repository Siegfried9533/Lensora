package com.example.backend.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class AssetResponse {
    private Long assetId;
    private String categoryName;
    private String modelName;
    private String brand;
    private String description;
    private Double price;
    private Double dailyRate;
    private Double depositValue;
    private String status;
    // private String serialNumber; // Có thể không cần thiết
    private List<String> imageUrls;
}
