package com.example.my_mobile_app.api.dto;

import java.util.List;

public class CreateOrderRequest {
    public String shippingAddress;
    public String paymentMethod;
    public Double shippingFee;
    public Boolean clearCart;
    public List<Item> items;

    // Dữ liệu người nhận + mã địa chỉ GHN để backend tạo vận đơn.
    // Tên trường phải khớp byte-for-byte với key backend đọc trong OrderController.
    public String recipientName;
    public String recipientPhone;
    public String toDistrictId;
    public String toWardCode;

    public CreateOrderRequest() {}

    public CreateOrderRequest(String shippingAddress, String paymentMethod, List<Item> items) {
        this.shippingAddress = shippingAddress;
        this.paymentMethod = paymentMethod;
        this.items = items;
    }

    public static class Item {
        public String productId;
        public int quantity;

        public Item() {}

        public Item(String productId, int quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }
    }
}
