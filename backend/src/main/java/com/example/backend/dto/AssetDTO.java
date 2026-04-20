package com.example.backend.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetDTO {
    private String assetId;
    private String categoryId;
    private String categoryName;
    private String userId;
    private String modelName;
    private String brand;
    private Long dailyRate;
    private String status;
    private String serialNumber;
    private List<String> imageUrls;
    private String primaryImageUrl;
}
