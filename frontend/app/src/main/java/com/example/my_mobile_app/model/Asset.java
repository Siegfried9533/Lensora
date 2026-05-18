package com.example.my_mobile_app.model;

import java.util.List;

/** Mirrors {@code Asset} in productApi.ts. */
public class Asset {
    public String assetId;
    public String categoryId;
    public String categoryName;
    public String userId;
    public String modelName;
    public String brand;
    public String description;
    public double dailyRate;
    /** AVAILABLE | RENTED | MAINTENANCE. */
    public String status;
    public String serialNumber;
    public List<String> imageUrls;
    public String primaryImageUrl;
    /** Some endpoints include a deposit amount; nullable. */
    public Double depositFee;

    public Asset() {}
}
