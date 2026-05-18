package com.example.my_mobile_app.api.dto;

public class CreateRentalRequest {
    public String assetId;
    public String startDate;
    public String endDate;
    public String shippingAddress;
    /** COD | MoMo. */
    public String paymentMethod;
    public Double shippingFee;

    public CreateRentalRequest() {}

    public CreateRentalRequest(String assetId, String startDate, String endDate) {
        this.assetId = assetId;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
