package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Assets")
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long assetId;

    @ManyToOne // Kết nối trực tiếp với bảng Category
    @JoinColumn(name = "categoryId")
    private Category category;

    private String modelName;
    private String brand;
    private String description;

    // thêm thuộc tính mới Price - giá trị gốc của tài sản cho thuê phục vụ tính
    // toán giá cọc
    private Double price;
    private Double dailyRate; // Giá thuê theo ngày
    private Double depositValue; // tiến cọc

    private String status; // 'AVAILABLE', 'RENTED', 'MAINTENANCE'
    private String serialNumber;

    @PrePersist
    @PreUpdate
    public void calculateDeposit() {
        if (this.depositValue == null) { // nếu chưa có giá trị cọc, tự động tính dựa trên giá gốc
            this.depositValue = this.price * 0.3; // Cọc 30% giá trị tài sản
        }
    }
}
