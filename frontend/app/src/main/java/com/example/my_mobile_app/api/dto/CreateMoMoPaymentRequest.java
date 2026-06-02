package com.example.my_mobile_app.api.dto;

public class CreateMoMoPaymentRequest {
    public String orderId;
    public long amount;
    public String orderInfo;
    /** captureWallet | payWithMethod | linkAndPay | linkAndPayWithToken. */
    public String requestType;

    public CreateMoMoPaymentRequest() {}

    public CreateMoMoPaymentRequest(String orderId, long amount, String orderInfo) {
        this.orderId = orderId;
        this.amount = amount;
        this.orderInfo = orderInfo;
    }
}
