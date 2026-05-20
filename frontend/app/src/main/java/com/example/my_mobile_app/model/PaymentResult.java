package com.example.my_mobile_app.model;

/** Mirrors {@code PaymentResult} in paymentApi.ts. */
public class PaymentResult {
    public boolean success;
    public String message;
    public String orderCode;
    public double amount;
    public String transactionRef;
    public String paymentMethod;

    public PaymentResult() {}
}
