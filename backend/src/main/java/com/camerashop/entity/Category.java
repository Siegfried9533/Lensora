package com.camerashop.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {
    @Id
    @Column(nullable = false, unique = true)
    private String categoryId;

    @Column(nullable = false)
    private String categoryName;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EntityType type;

    public enum EntityType {
        PRODUCT, ASSET
    }
}
