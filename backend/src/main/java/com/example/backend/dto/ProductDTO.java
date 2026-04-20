package com.example.backend.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {
    private String productId;
    private String categoryId;
    private String categoryName;
    private String userId;
    private String productName;
    private String brand;
    private String description;
    private Long price;
    private Integer stockQuantity;
    private List<String> imageUrls;
    private String primaryImageUrl;
}
