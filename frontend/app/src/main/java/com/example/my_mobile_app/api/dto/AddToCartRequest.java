package com.example.my_mobile_app.api.dto;

public class AddToCartRequest {
    public String itemId;
    /** PRODUCT | ASSET. */
    public String type;
    public int quantity;
    /** Rental only. */
    public String startDate;
    public String endDate;

    public AddToCartRequest() {}

    public AddToCartRequest(String itemId, String type, int quantity) {
        this.itemId = itemId;
        this.type = type;
        this.quantity = quantity;
    }

    public AddToCartRequest(String itemId, String type, int quantity, String startDate, String endDate) {
        this(itemId, type, quantity);
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
