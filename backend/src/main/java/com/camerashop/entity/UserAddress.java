package com.camerashop.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * A shipping address saved in a user's address book. A user may have many; exactly one is the
 * default. Stores GHN district_id / ward_code so the checkout can recompute the shipping fee
 * without re-resolving the location.
 */
@Entity
@Table(name = "user_addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAddress extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String addressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String recipientName;

    @Column(nullable = false)
    private String recipientPhone;

    private String provinceId;
    private String provinceName;
    private String districtId;
    private String districtName;
    private String wardCode;
    private String wardName;
    private String street;
    private String note;

    @Builder.Default
    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;
}
