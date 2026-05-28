package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "images")
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long imageId;

    @Column(name = "entity_id")
    private Long entityId; // productId or assetId

    @Column(name = "url")
    private String url;

    @Column(name = "is_primary")
    private Boolean isPrimary;

    @Column(name = "type")
    private String type; // 'PRODUCT' | 'ASSET'

    public Image() {
    }

    public Image(Long imageId, Long entityId, String url, Boolean isPrimary, String type) {
        this.imageId = imageId;
        this.entityId = entityId;
        this.url = url;
        this.isPrimary = isPrimary;
        this.type = type;
    }

    // Getters and setters
    public Long getImageId() {
        return imageId;
    }

    public void setImageId(Long imageId) {
        this.imageId = imageId;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean getIsPrimary() {
        return isPrimary;
    }

    public void setIsPrimary(Boolean isPrimary) {
        this.isPrimary = isPrimary;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
