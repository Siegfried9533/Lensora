package com.example.my_mobile_app.model;

/** Mirrors {@code Favorite} in favoriteApi.ts. */
public class Favorite {
    public String favoriteId;
    public String productId;
    public String assetId;
    /** PRODUCT | ASSET. */
    public String type;
    public String productName;
    public String assetName;
    public Double price;
    public String primaryImageUrl;

    public Favorite() {}
}
