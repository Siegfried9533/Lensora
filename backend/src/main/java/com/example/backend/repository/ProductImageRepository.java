package com.example.backend.repository;

import com.example.backend.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    @Query("SELECT pi FROM ProductImage pi WHERE pi.product.id = :productId")
    List<ProductImage> findByProductId(@Param("productId") Long productId);

    @Query("SELECT pi FROM ProductImage pi WHERE pi.product.id = :productId AND pi.isPrimary = true")
    ProductImage findByProductIdAndIsPrimaryTrue(@Param("productId") Long productId);
}
