package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "asset_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetImage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String imageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isPrimary = false;
}
