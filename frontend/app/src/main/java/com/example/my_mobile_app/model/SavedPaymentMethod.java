package com.example.my_mobile_app.model;

/** Mirrors {@code PaymentMethodDTO} on the backend. Metadata only — no raw account numbers. */
public class SavedPaymentMethod {
    public String paymentMethodId;
    /** MOMO | BANK. */
    public String type;
    public String label;
    public String accountHolder;
    public String maskedAccount;
    public boolean isDefault;

    public SavedPaymentMethod() {}
}
