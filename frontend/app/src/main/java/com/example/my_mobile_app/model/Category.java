package com.example.my_mobile_app.model;

/** Mirrors {@code Category} in productApi.ts (PRODUCT|ASSET). */
public class Category {
    public String categoryId;
    public String categoryName;
    /** "PRODUCT" or "ASSET". */
    public String type;

    public Category() {}
}
