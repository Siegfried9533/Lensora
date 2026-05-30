package com.example.my_mobile_app.model;

/** Mirrors {@code Rental} in orderApi.ts. */
public class Rental {
    public String rentalId;
    public String userId;
    public String assetId;
    public String assetName;
    public String assetBrand;
    public String primaryImageUrl;
    public String startDate;
    public String endDate;
    public String returnDate;
    public double depositFee;
    public double totalRentFee;
    public double penaltyFee;
    /** PENDING | ACTIVE | COMPLETED | CANCELLED. */
    public String status;
    public String shippingAddress;
    public String paymentMethod;
    public Long shippingFee;
    /** PENDING | SUCCESS | FAILED | EXPIRED. */
    public String paymentStatus;

    public Rental() {}
}
