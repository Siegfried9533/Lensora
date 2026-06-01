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
    /** Cảnh báo khi tạo vận đơn GHN thất bại (đơn vẫn được tạo thành công). */
    private String ghnWarning;
    /** Lý do hủy đơn (nếu đã hủy). */
    private String cancelReason;
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