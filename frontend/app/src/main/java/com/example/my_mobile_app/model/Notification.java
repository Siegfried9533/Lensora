package com.example.my_mobile_app.model;

/** Mirrors {@code Notification} in notificationApi.ts. */
public class Notification {
    public String notificationId;
    public String userId;
    public String title;
    public String message;
    /** ORDER_UPDATE|RENTAL_REMINDER|RENTAL_OVERDUE|SYSTEM|PROMOTION|PAYMENT_SUCCESS|PAYMENT_FAILED|SHIPPING_UPDATE. */
    public String type;
    public String referenceId;
    /** ORDER | RENTAL. */
    public String referenceType;
    public boolean isRead;
    public boolean isActionRequired;
    public String actionUrl;
    public String deepLinkUrl;
    public String createdAt;
    public String readAt;

    public Notification() {}
}
