package com.example.my_mobile_app.api.dto;

public class CreateMoMoPaymentRequest {
    public String orderId;
    public double amount;
    public String orderInfo;
    /** captureWallet | payWithMethod | linkAndPay | linkAndPayWithToken. */
    public String requestType;

    public CreateMoMoPaymentRequest() {}

    public CreateMoMoPaymentRequest(String orderId, double amount, String orderInfo) {
        this.orderId = orderId;
        this.amount = amount;
        this.orderInfo = orderInfo;
    }
}
