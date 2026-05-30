package com.camerashop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * A payment method a user has saved in settings (e.g. a bank account or a MoMo wallet).
 *
 * Stores display metadata only — never raw card numbers, CVV, or credentials. The
 * {@code maskedAccount} holds an already-masked identifier such as "**** 1234".
 */
@Entity
@Table(name = "payment_methods")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavedPaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String paymentMethodId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private MethodType type;

    @Column(nullable = false)
    private String label;

    private String accountHolder;

    private String maskedAccount;

    @Builder.Default
    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public enum MethodType {
        MOMO, BANK
    }
}
