package com.camerashop.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity for user-specific notifications
 * Used for order updates, rental return reminders, and system notifications
 */
@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_user_unread", columnList = "user_id, is_read"),
    @Index(name = "idx_reference", columnList = "reference_id, reference_type"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @Column(length = 36)
    private String notificationId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    @Column(length = 36)
    private String referenceId;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private ReferenceType referenceType;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActionRequired = false;

    @Column(length = 500)
    private String actionUrl;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime readAt;

    @Column
    private LocalDateTime expiresAt;

    /**
     * Notification types
     */
    public enum NotificationType {
        ORDER_UPDATE,        // Order status changed
        RENTAL_REMINDER,     // Rental return date approaching
        RENTAL_OVERDUE,      // Rental is overdue
        SYSTEM,              // System-wide announcement
        PROMOTION,           // Promotional offers
        PAYMENT_SUCCESS,     // Payment confirmed
        PAYMENT_FAILED,      // Payment failed
        SHIPPING_UPDATE      // Shipping status changed
    }

    /**
     * Reference types for linking notifications to entities
     */
    public enum ReferenceType {
        ORDER,
        RENTAL
    }

    /**
     * Mark notification as read
     */
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }

    /**
     * Check if notification is expired
     */
    public boolean isExpired() {
        if (expiresAt == null) return false;
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Get deep link route for mobile app
     */
    public String getDeepLinkUrl() {
        if (actionUrl != null) {
            return actionUrl;
        }

        // Generate default deep link based on reference type
        if (referenceType == ReferenceType.ORDER && referenceId != null) {
            return "mobile://order-details?id=" + referenceId;
        } else if (referenceType == ReferenceType.RENTAL && referenceId != null) {
            return "mobile://rental-details?id=" + referenceId;
        }

        return "mobile://notifications";
    }
}
