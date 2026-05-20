package com.example.my_mobile_app.ui.home;

import com.example.my_mobile_app.model.Asset;
import com.example.my_mobile_app.model.Product;

/**
 * Unified row model for the Discovery list, wrapping either a {@link Product}
 * or an {@link Asset} so a single RecyclerView adapter can render both.
 */
public class DisplayItem {
    public String id;
    public String title;
    public double price;
    /** "PRODUCT" or "ASSET". */
    public String type;
    public String categoryName;
    public String primaryImageUrl;
    public String categoryId;

    public DisplayItem() {}

    public static DisplayItem from(Product p) {
        DisplayItem d = new DisplayItem();
        d.id = p.productId;
        d.title = p.productName;
        d.price = p.price;
        d.type = "PRODUCT";
        d.categoryName = p.categoryName;
        d.primaryImageUrl = p.primaryImageUrl;
        d.categoryId = p.categoryId;
        return d;
    }

    public static DisplayItem from(Asset a) {
        DisplayItem d = new DisplayItem();
        d.id = a.assetId;
        d.title = a.modelName;
        d.price = a.dailyRate;
        d.type = "ASSET";
        d.categoryName = a.categoryName;
        d.primaryImageUrl = a.primaryImageUrl;
        d.categoryId = a.categoryId;
        return d;
    }
}
