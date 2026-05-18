package com.example.my_mobile_app.model;

import java.util.List;

/** Mirrors {@code Product} in productApi.ts. */
public class Product {
    public String productId;
    public String categoryId;
    public String categoryName;
    public String userId;
    public String productName;
    public String brand;
    public String description;
    public double price;
    public int stockQuantity;
    public List<String> imageUrls;
    public String primaryImageUrl;

    public Product() {}
}
