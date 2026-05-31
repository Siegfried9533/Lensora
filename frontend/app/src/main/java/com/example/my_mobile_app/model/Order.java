package com.example.my_mobile_app.model;

import java.util.List;

/** Mirrors {@code Order} in orderApi.ts. */
public class Order {
    public String orderId;
    public String userId;
    /** ISO-8601 date string. */
    public String orderDate;
    public double totalAmount;
    public String shippingAddress;
    /** PENDING|CONFIRMED|PROCESSING|SHIPPED|DELIVERED|CANCELLED. */
    public String status;
    /** COD | VNPay (also MoMo in some flows). */
    public String paymentMethod;
    /** PENDING | SUCCESS | FAILED. */
    public String paymentStatus;
    public Double shippingFee;
    public String ghnOrderId;
    /** Cảnh báo khi backend tạo vận đơn GHN thất bại (đơn vẫn được tạo). Chỉ có ở phản hồi tạo đơn. */
    public String ghnWarning;
    public List<OrderItem> orderItems;

    public Order() {}
}
