package com.camerashop.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {
    private String orderId;
    private String userId;
    private LocalDateTime orderDate;
    private Long totalAmount;
    private String shippingAddress;
    private String status;
    private String paymentMethod;
    private String paymentStatus;
    private Long shippingFee;
    private String ghnOrderId;
    private List<OrderItemDTO> orderItems;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemDTO {
        private String productName;
        private Integer quantity;
        private Long priceAtPurchase;
        private String imageUrl;
    }
}
