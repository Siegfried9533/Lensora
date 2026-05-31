package com.camerashop.dto;

import lombok.*;
import java.time.LocalDateTime;

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
    private LocalDateTime createdAt;
    // Computed fields
    private String productName;
    private String assetName;
    private Long price;
    private String primaryImageUrl;
}
