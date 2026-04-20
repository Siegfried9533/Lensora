package com.example.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemDTO {
    private String cartItemId;
    private String productId;
    private String assetId;
    private Integer quantity;
    private String type;
    // Computed fields
    private String productName;
    private String assetName;
    private Long price;
    private String primaryImageUrl;
}
