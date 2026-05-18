package com.example.my_mobile_app.model;

/** Mirrors {@code CartItem} in cartApi.ts. */
public class CartItem {
    public String cartItemId;
    public String productId;
    public String assetId;
    public int quantity;
    /** PRODUCT | ASSET. */
    public String type;
    public String productName;
    public String assetName;
    public Double price;
    public String primaryImageUrl;
    /** Rental cart entries only. */
    public String startDate;
    public String endDate;

    public CartItem() {}
}
