package com.camerashop.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteDTO {
    private String favoriteId;
    private String productId;
    private String assetId;
    private String type;
    // Computed fields
    private String productName;
    private String assetName;
    private Long price;
    private String primaryImageUrl;
}
